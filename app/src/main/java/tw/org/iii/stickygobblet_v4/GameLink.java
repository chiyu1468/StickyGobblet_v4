package tw.org.iii.stickygobblet_v4;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */

public class GameLink{
    FirebaseDatabase database;
    DatabaseReference gameRef;

    boolean isEnd;

    private String key;
    private GameActivity parentContext;
    // 一個區域 儲存網路上的遊戲狀態
    GameOnNet downloadGameOnNet;

    public GameLink(Context context,final String myName) {
        this(context,myName,null);
    }

    public GameLink(Context context,final String myName, String givenKey) {
        // 房客會先拿到 房間的 key
        key = givenKey;

        parentContext = (GameActivity) context;
        parentContext.tv.setText("Wait Connect to Firebase!!");

        database = FirebaseDatabase.getInstance();
        gameRef = database.getReference("StickyGobblet");
        // 驗證連線
        gameRef.child("LinkTest").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue().toString().equals("OK")) {
                    connect = true;
                    // 確認連線後

                    if(parentContext.myFaction == 1) {
                        // 如果是創造房主模式
                        // 取得房間鑰匙 後創立房間
                        key = gameRef.child("Waiting").push().getKey();
                        createRoom(myName);
                    } else if(parentContext.myFaction == 2) {
                        // 房客
                        joinRoom(myName);
                    } else if(parentContext.myFaction == 3) {
                        // 單機版
                        key = gameRef.child("Waiting").push().getKey();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }

    // 檢測連線狀態
    private boolean connect;
    boolean isConnect() {return connect;}

    // 開新房間
    private void createRoom(String name1){
        parentContext.tv.setText("Wait player2 join!!");
        // 等待區房間 底下寫自己的名字
        gameRef.child("/Waiting/" + key + "/host/").setValue(name1);
        // 監聽是否有人加入
        gameRef.child("/Waiting/" + key + "/client/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name2 = dataSnapshot.getValue(String.class);
                if(name2 != null) detectClientJoin(name2);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    // 偵測到有玩家加入後 把房間移出等待區
    // 加到遊戲區域 設定 gameState 為 gameSync
    private void detectClientJoin(String name2) {
        parentContext.playersName[2] = name2;
        parentContext.playersArrived();
        gameRef.child("/Waiting/" + key).setValue(null);
        // TODO BUG to fix
//        gameRef.child("/Playing/" + key + "/player1/").setValue("OK");
        //gameRef.child("/Playing/" + key + "/player2/" + parentContext.playersName[2]).setValue("OK");
        uploadPlayingGame(new GameOnNet(GameOnNet.GameState.gameSync,
                parentContext.gameCore.getNowPlayerID(),
                parentContext.gameCore.gameCheckerBoard.gameRecode));
        // 開始遊戲
        onlineGameStart();
    }

    private void joinRoom(final String name2) {
        // 取得房主的名稱
        // 加入房間 寫入自己的名字
        gameRef.child("/Waiting/" + key + "/host/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                parentContext.playersName[1] = dataSnapshot.getValue(String.class);
                gameRef.child("/Waiting/" + key + "/client/").setValue(name2);
                // 開始遊戲
                parentContext.playersArrived();
                onlineGameStart();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // 遊戲開始掛上遊戲狀態的監聽器
    onlineGameLinstener gameLinstener;
    private void onlineGameStart() {
        downloadGameOnNet = new GameOnNet();
        gameLinstener = new onlineGameLinstener();
        gameRef.child("/Playing/" + key).addValueEventListener(gameLinstener);
    }

    // 這裡處理遊戲監聽功能
    class onlineGameLinstener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            //for(DataSnapshot dd : dataSnapshot.getChildren()) {Log.v("chiyu","key : " + dd.getKey());}

            // 可能會有時間延遲 加這行比較不會出錯
            if(dataSnapshot.hasChild("gameState")) {

                downloadGameOnNet.nowPlayer = dataSnapshot.child("nowPlayer").getValue(Integer.class);
                downloadGameOnNet.gameState = dataSnapshot.child("gameState").getValue(Integer.class);
                //downloadGameOnNet.gameRecode = dataSnapshot.child("gameRecode").getValue(LinkedList.class);

                // 由 gameState 判斷現在的狀態
                switch (downloadGameOnNet.gameState) {
                    case GameOnNet.GameState.gameSync:
                        if(!dataSnapshot.hasChild("gameRecode") && (parentContext.myFaction == 2)) {
                            // 棋局一開始 房客需要同步先後手順序
                            parentContext.gameCore.setNowPlayerID(downloadGameOnNet.nowPlayer);
                            // 同步完成 則變更遊戲狀態
                            // gameRef.child("/Playing/" + key + "/gameState/").setValue(GameOnNet.GameState.waitPlayer);
                        } else if(dataSnapshot.hasChild("gameRecode")){
                            // 棋局中 則解碼遊戲紀錄 並同步棋局
                            decodeGameRecode(dataSnapshot);
                        }
                        // 同步完成 就改變 gameState 這樣自己才可以下子
                        gameRef.child("/Playing/" + key + "/gameState/").setValue(GameOnNet.GameState.waitPlayer);
                        break;
                    case GameOnNet.GameState.waitPlayer:
                        String tName = parentContext.gameCore.GO();
                        if(parentContext.gameCore.gameCheckerBoard.getPassedTurns() == 0)
                            tName = "Game Start!!!\n" + tName;
                        parentContext.tv.setText(tName + "'s turn.");
                        //LinkMessage = tName + "'s turn";
                        break;
                    case GameOnNet.GameState.gameOver:
                        isEnd = true;
                        if(dataSnapshot.hasChild("gameRecode"))
                            decodeGameRecode(dataSnapshot);

                        if(parentContext.myFaction != 2)
                            gameRef.child("/Ending/" + key + "/gameRecode/").setValue(
                                    dataSnapshot.child("/gameRecode/").getValue());

                        break;
                }


            }

            //GameOnNet gameOnNet = dataSnapshot.getValue(GameOnNet.class);
            //Log.v("chiyu","onDataChange "+ gameOnNet.nowPlayer);
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {}
    }

    // 監聽到對手有動作 用這裡解讀
    void decodeGameRecode(DataSnapshot dataSnapshot) {
        int turn = parentContext.gameCore.gameCheckerBoard.getPassedTurns();
        ArrayList<HashMap<String,Long>> gameRecode =
                (ArrayList<HashMap<String,Long>>)
                        dataSnapshot.child("gameRecode").getValue();
        if(gameRecode.size() > turn) {
            // 解碼
            // 取得最後一筆資料
            // 注意 firebase 讀回來 不知為何 都會變成Long
            long target = gameRecode.get(gameRecode.size()-1).get("target");
            long destination = gameRecode.get(gameRecode.size()-1).get("destination");
            long pieceInfo = gameRecode.get(gameRecode.size()-1).get("pieceInfo");
            long size = (pieceInfo / 16) % 16;
            String faction;
            switch ((int)pieceInfo % 16) {
                case 1:
                    faction = "BLUE";
                    break;
                case 2:
                    faction = "ORANGE";
                    break;
                default:
                    gameRef.child("/Playing/" + key + "/gameState/").setValue(GameOnNet.GameState.fatalError);
                    faction = null;
                    break;
            }



            Log.v("chiyu", "Rival Move, size : " + size +
                    ",target : " + target +
                    ",destination : " + destination +
                    ",faction : " + faction);

            // 同步棋步
            parentContext.netControlGame(faction,(int)target,(int)destination,(int)size);

        }

    }

    // 自己有動作 用這個上傳遊戲狀態
    public void uploadPlayingGame(GameOnNet gameOnNet) {
        gameRef.child("/Playing/" + key).setValue(gameOnNet);
    }

    public void setGameOver() {
        gameRef.child("/Playing/" + key + "/gameState/").setValue(GameOnNet.GameState.gameOver);
    }
}

// 遊戲狀態類別 上傳遊戲狀態用
class GameOnNet {
    // 遊戲狀態
    // gameSync 等待雙方同步
    // waitPlayer 等待玩家動作
    // waitPlayer 遊戲結束
    int gameState;
    class GameState {
        final static int gameSync = 0;
        final static int waitPlayer = 1;
        final static int gameOver = 2;
        final static int fatalError = 3;
    }

    int nowPlayer;
    LinkedList<HashMap<String,Integer>> gameRecode;

    GameOnNet(int gameState, int nowPlayer, LinkedList gameRecode) {
        this.gameState = gameState;
        this.nowPlayer = nowPlayer;
        this.gameRecode = gameRecode;
    }

    GameOnNet() {}
}