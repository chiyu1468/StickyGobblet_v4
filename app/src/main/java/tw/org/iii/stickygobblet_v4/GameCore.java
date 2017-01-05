package tw.org.iii.stickygobblet_v4;

import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * 遊戲核心
 * - GameCore
 *     -> private 方法： 初始化  先後手  狀態機(StateMachine)  處理錯誤碼
 *
 *     -> public 方法：  玩家加入(創建+分配棋子+分配陣營)  啟動(GO)
 *                      玩家移動棋子  玩家放新子(playerMove)
 *     -> public 屬性：  要傳遞出去的訊息
 *
 * - checkerboard
 *     -> private 屬性： 回合數  各方格棋子堆疊
 *     -> private 方法： 初始化棋盤  下子push  提子pop
 *                      讀取棋盤目前最外層棋子狀態  移動棋子  將新子放入暫存格
 *
 *     -> private 功能： 棋步紀錄(增加回合數)
 *
 * - piece
 *     -> private 屬性： 棋種  陣營  ID
 *
 *     -> friend 方法： 取得棋子資料(大小 陣營 總和)
 *
 * - player
 *     -> private 屬性： 陣營  棋庫(手上的棋子)  創造新棋
 *
 *     -> public 方法： 查詢棋子剩餘(amountInStore)
 *
 *
 * - gameReferee
 *     -> 屬性：
 *     -> 方法： 判斷勝負並計算game point(judge ＆ isGameWin)  判斷重複
 *
 *
 */


// GameCore include two part
// first part, declare static constant that game need
// second part, game engine
public class GameCore {

    // 棋盤大小
    static final byte BoardLength = 3;
    static final byte BoardHeight = BoardLength;
    static final byte BoardWidth = BoardLength;
    static final int BoardArea = BoardHeight * BoardWidth;

    // 遊戲人數
    static final int PlayerNum = 2;

    // 陣營的種類
    static final String[] factionName = {"null","Blue","Orange"};
    // 棋子的種類
    static final String[] sizeName = {"null","Large","Middle","Small"};
    // 每種棋子分配的數量
    static final int PieceRation = 2;

    //=================================================================================

    // 遊戲要一直用到的靈魂角色
    HashMap<Integer,player> playerList;
    checkerboard gameCheckerBoard;
    gameReferee MrX;

    // 要傳遞出去的訊息
    public String message = "";

    // State Machine 用的參數
    final int state_Initial = 900;
    final int state_WaitPlayerJoin = 901;
    final int state_readyToStart = 902;
    final int state_PlayerTurn = 903;
    final int state_Referee = 904;
    final int state_End = 905;
    private int nowState = 0;

    // 用於判斷棋步合法用的參數
    private byte nowFaction = 0;
    private int nowPlayerID = 0;

    GameCore() {nowState = state_Initial;}  // 初始化 state

    // State Machine
    private void StateMachine() {
        switch (nowState) {
            case state_Initial:
                Log.v("chiyu","state_Initial");// trailrun
                Initial();
                nowState = state_WaitPlayerJoin;
                break;
            case state_WaitPlayerJoin:
                Log.v("chiyu","state_WaitPlayerJoin");// trailrun
                // 不幹嘛 就是等玩家到齊
                if(playerList.size() != PlayerNum) {
                    Log.v("chiyu","Please Use ‘addPlayer’ Method add Player");// trailrun
                    break;
                }
                nowState = state_readyToStart;
                message = "Wait Player Ready~";
                // 到齊後 就繼續執行下一個 state
            case state_readyToStart:
                Log.v("chiyu","state_readyToStart");// trailrun
                Precedence();  // 分配先後手
                if(nowPlayerID == 0) break;
                message = "Game Start!!!\n";
                nowState = state_PlayerTurn;
                // 分配完成後 就繼續執行下一個 state
            case state_PlayerTurn:
                Log.v("chiyu","state_PlayerTurn");// trailrun
                message = message + playerList.get(nowPlayerID).getName() + "'s turn.";
                // 等玩家給指令
                // state 的改變放在 playerMove 裡
                break;
            case state_Referee:
                Log.v("chiyu","state_Referee");// trailrun
                // 出現贏家
                if(MrX.judge(gameCheckerBoard) == -1) nowState = state_End;
                else {
                    nowPlayerID = (nowPlayerID % playerList.size()) + 1;
                    message = "";
                    nowState = state_PlayerTurn;
                    break;
                }
            case state_End:
                Log.v("chiyu","state_End"); // trailrun
                // 遊戲結束
                // 陣營=null 讓所有人都不能操作
                nowPlayerID = 0;
                break;
            default:
                break;
        }
    }

    // 初始化
    private void Initial() {
        // 拿出一個新棋盤
        gameCheckerBoard = new checkerboard();
        // 叫來一個裁判
        MrX = new gameReferee();
        // 玩家列表
        playerList = new HashMap<>();
        // 初始化結束 --> 等待玩家加入 state
    }

    // 分配誰先手
    private void Precedence() {
        // 隨機挑選一個玩家為先手
        nowPlayerID = (int)( Math.random() * playerList.size() ) + 1;
    }

    // 處理錯誤碼
    private void ProcessErrorCode(){
//        Iterator<Integer> EC;
//        while (!EList.isEmpty()) {
//            EC = EList.iterator();
//            switch (EC.next()){
//
//            }
//        }
        Log.v("chiyu","Error List : " + EList.toString());
        EList.clear();
    }

    // =====================================
    // 給玩家使用的指令
    // 呼叫狀態機 以及處理錯誤訊息
    String GO() {
        StateMachine();
        ProcessErrorCode();

        if(nowPlayerID > 0) return playerList.get(nowPlayerID).getName();
        return "";
    }
    // 玩家加入
    boolean addPlayer(String name) {
        // 判斷人數是否滿
        if(playerList.size() < PlayerNum) {
            // 加入玩家
            playerList.put(
                    playerList.size() + 1,
                    new player(
                            playerList.size() + 1,
                            (byte) (playerList.size() + 1),
                            PieceRation,
                            name));
            return true;
        } else {return false;}
    }
    // 移動
    boolean playerMove(String name, int target, int destination) {
        // 判斷是否為 玩家的 state
        if(nowState != state_PlayerTurn) {
            EList.add(ErrorCode.stateError);
            return false;
        }
        // 判斷是否為該玩家的回合
        if(playerList.get(nowPlayerID).getName().equals(name)) {
            // 設定目前玩家的陣營
            nowFaction = playerList.get(nowPlayerID).getFaction();
            // 判斷這步棋有沒有成立
            if(!gameCheckerBoard.movePiece(target,destination)) return false;
            // 完成棋子移動後 改變 state
            nowState = state_Referee;
        } else {
            EList.add(ErrorCode.nowPlayerIDNotMatch);
            return false;
        }
        StateMachine();
        ProcessErrorCode();
        return true;
    }
    // 放新子
    boolean playerMove(String name, int noUse, int destination, byte size) {
        noUse = 0;
        // 判斷是否為 玩家的 state
        if(nowState != state_PlayerTurn) {
            EList.add(ErrorCode.stateError);
            return false;
        }
        // 判斷是否為該玩家的回合
        if(playerList.get(nowPlayerID).getName().equals(name)) {
            // 設定目前玩家的陣營
            nowFaction = playerList.get(nowPlayerID).getFaction();
            // 如果要放入的棋子 大於 目的地的棋子大小
            if(size > gameCheckerBoard.getSize(destination)) {
                // 就創建新棋子
                gameCheckerBoard.putInPiece(
                        playerList.get(nowPlayerID).createNewPiece(size));
                // 並放到定位
                if(!gameCheckerBoard.movePiece(0,destination)) return false;
            } else {
                EList.add(ErrorCode.sizeErrorPiece);
                return false;
            }
            // 完成棋子移動後 改變 state
            nowState = state_Referee;
        } else {
            EList.add(ErrorCode.nowPlayerIDNotMatch);
            return false;
        }
        StateMachine();
        ProcessErrorCode();
        return true;
    }
    // 取得玩家ID 上傳遊戲用的
    int getNowPlayerID(){return nowPlayerID;}
    // 讓房客 設定先手的玩家用的
    boolean setNowPlayerID(int playerID){
        if(gameCheckerBoard.gameRecode.size() != 0){
            // 不是起始棋局了
            EList.add(ErrorCode.changeNowPlayerIDwhilePlaying);
            return false;}
        nowPlayerID = playerID;
        return true;
    }

    // 強制設定回合
    void TrailRun() {
        nowPlayerID = 2;
    }
    // 顯示目前狀態
    void TrailRun2() {
        MrX.readBoard(gameCheckerBoard);
        MrX.RefereeTest();
    }
    // 建置預設棋盤
    void TrailRun3() {
        gameCheckerBoard.testpushpop();
    }


    // ============== 類別 ================

    class checkerboard implements Serializable {
        // 目前遊戲回合數
        private int passedTurns;
        int getPassedTurns(){return passedTurns;}

        // 棋盤面 = 方格 ＋ 棋子堆疊
        // 用 整數 對方格編號
        // 用 LinkedList<piece> 紀錄目前有哪些棋子 在這個方格
        private HashMap<Integer, LinkedList<piece>> boardGrids;

        // 建構式 就跑初始化
        checkerboard() {boardInitial();}
        // 初始化
        private void boardInitial() {
            // 新的紀錄
            gameRecode = new LinkedList<>();
            passedTurns = 0;
            // 創造棋格 (棋格 0 => 新棋暫存格)
            boardGrids = new HashMap<>();
            // 初始化 每個棋格 墊底一個無效棋子
            for(int counter = 0; counter <= (GameCore.BoardArea) ; counter++) {
                LinkedList<piece> grid = new LinkedList<>();
                grid.add(new piece((byte) 0,(byte) 0,0));
                boardGrids.put(counter,grid);
            }
//        LinkedList<piece> grid1 = boardGrids.get(9); // trail run
//        piece A = grid1.get(0);  // trail run
//        Log.v("chiyu","trail run " + A.getPieceInfo());  // trail run
        }

        // 棋步紀錄
        // history 類別 => 回合數,陣營,棋子ＩＤ,選擇棋格,目標棋格
        //LinkedList<history> gameRecode; // 改掉用 HashMap 了
        LinkedList<HashMap> gameRecode;
//        class history {
//                int turn;
//                int pieceInfo;
//                int target, destination;
//                history (piece nowPiece, int from,int to) {
//                    turn = passedTurns;
//                    pieceInfo = nowPiece.getPieceInfo();
//                    //TODO target destination
//                    target = from;
//                    destination = to;
//                }
//        }

        // Recoder 用 add 的方式將每個回合的 history 物件 加到 LinkedList<>
        // 每次紀錄 回合加一
        private void Recoder(piece nowPiece, int from, int to) {
            HashMap<String, Integer> history = new HashMap<>();
            history.put("turn",passedTurns);
            history.put("pieceInfo",nowPiece.getPieceInfo());
            history.put("target",from);
            history.put("destination",to);
            //gameRecode.add(new history(nowPiece, from, to)); // 改掉用 HashMap 了
            gameRecode.add(history);
            passedTurns ++;
        }

        // 下子 to 棋格
        private boolean depositPiece(int destination, piece targetPiece) {
            // 從棋格先找出 LinkedList 棋子堆疊
            LinkedList<piece> destGrid = boardGrids.get(destination);
            // 判斷是否拿到無效棋子
            if(targetPiece.getPieceInfo() != 0) {
                // 判斷下面的棋子 是不是小於要放上去的
                if(destGrid.getFirst().getSize() < targetPiece.getSize()) {
                    // 把棋子push進堆疊最上面
                    destGrid.push(targetPiece);
                    // 把 LinkedList 棋子堆疊 複寫進棋格
                    boardGrids.put(destination, destGrid);
                } else {
                    // 放不上去
                    EList.add(ErrorCode.sizeErrorPiece);
                    return false;
                }
            }else {
                // 拿到無效棋子
                EList.add(ErrorCode.depositVoidPiece);
                return false;
            }
            return true;
        }

        // 提子 from 棋格
        private piece withdrawPiece(int target) {
            // 從棋格先找出 LinkedList 棋子堆疊
            LinkedList<piece> targetGrid = boardGrids.get(target);
            // pop出堆疊最上面的棋子
            piece targetPiece = targetGrid.pop();
            // 判斷是否抓到 墊底用的 無效棋子
            if(targetPiece.getSize() == 0){
                // 將棋子push回去 不然會出錯
                targetGrid.push(targetPiece); // debug v2
                // 標記 操作無效棋子
                EList.add(ErrorCode.withdrawVoidPiece);
                // 判斷拿到的棋子 與 這個回合的陣營 不同
            } else if(targetPiece.getFaction() != nowFaction) {
                // 將棋子push回去 不然會出錯
                targetGrid.push(targetPiece); // debug v2
                targetPiece = new piece((byte)0,(byte)0,0);
                // 標記 拿到錯誤陣營的棋
                EList.add(ErrorCode.pieceWithErrorFaction);
            } else {
                // Log.v("chiyu","take piece"); // debug v2
                // 如果抓到有效棋子 才複寫棋格
                // 把 LinkedList 棋子堆疊 複寫進棋格
                // debug v2
                // 以上觀念錯誤 pop 出來的東西 是指針
                // 只要 pop 出來就會影響到 LinkedList 內容
                // boardGrids.put(target,targetGrid);
            }
            // 回傳 棋子
            return targetPiece;
        }

        // 以下方法是給其他類別存取的

        // 移動棋子 並紀錄動作 有成功 才回傳 true
        private boolean movePiece(int target,int destination) {
            // 如果棋子會被放到暫存格 就跳出
            if(destination == 0) {
                EList.add(ErrorCode.pieceMoveIntoGridZero);
                return false;
            }
            // 取出要移動的棋子
            piece mP = withdrawPiece(target);
            // 如果出現無效棋子錯誤 回傳 false
            if(mP.getSize() == 0) return false;
            // 放到棋格裡面
            // 如果 false 代表棋子沒有放成功
            if(!depositPiece(destination,mP)) {
                // 將棋子放回去
                depositPiece(target,mP);
                // 回傳 false
                return false;
            }
            // 棋子移動後 就紀錄這次的動作
            Recoder(mP,target,destination);
            return true;
        }

        // 把棋子放入暫存格子
        private void putInPiece(piece nP) {
            // 判斷是否拿到有效棋子
            if(nP.getSize() != 0) {
                depositPiece(0, nP);
            }
        }

        // 讀取棋盤目前最外層棋子狀態 (gameReferee會用到)
        byte getSize(int grid) {return boardGrids.get(grid).getFirst().getSize();}
        byte getFaction(int grid) {return boardGrids.get(grid).getFirst().getFaction();}

        // 測試用
        void testpushpop(){
            depositPiece(1,new piece((byte) 1,(byte) 1, 3));
            depositPiece(1,new piece((byte) 2,(byte) 2, 5));
            depositPiece(1,new piece((byte) 3,(byte) 1, 6));
            depositPiece(4,new piece((byte) 1,(byte) 1, 30));
            depositPiece(5,new piece((byte) 3,(byte) 2, 23));
            depositPiece(9,new piece((byte) 2,(byte) 2, 17));
            //depositPiece(9,new piece((byte) 2,(byte) 2, 17));






            // >>>>>>>> LinkedList 測試 <<<<<<<<
//        LinkedList<String> test = new LinkedList<>();
//        test.push("hello");
//        Log.v("chiyu",test.size() + " : push hello");
//        test.push("world");
//        Log.v("chiyu",test.size() + " : push world");
//        test.push("cute");
//        Log.v("chiyu",test.size() + " : push cute");
//        // 先進後出原則
//        Log.v("chiyu",test.pop() + test.size());
//        Log.v("chiyu",test.pop() + test.size());
//        Log.v("chiyu",test.pop() + test.size());

            // >>>>>>>> 測試 HashMap <<<<<<<<
//        // 基本測試
//        HashMap<Integer,String> testhm = new HashMap<>();
//        testhm.put(1,"hello");
//        testhm.put(2,"world");
//        testhm.put(3,"cute cute");
//        Log.v("chiyu",testhm.size() + "");
//
//        // 取出測試
//        String s;
//        s = testhm.get(2);
//        Log.v("chiyu",s);
//
//        // 用 keySet 可以管理所有的key
//        Set<Integer> keyset = testhm.keySet();
//        Log.v("chiyu",keyset.toString());
//        testhm.put(4,"loli loli");
//        // 不過 這邊看出 hash map 不會排序
//        Log.v("chiyu",keyset.toString());
//
//        // 如果東西從 keySet 移除掉 原來map的東西 也會不見
//        keyset.remove(2);
//        Log.v("chiyu",keyset.toString());
//        Log.v("chiyu",testhm.size() + "");
//        try {
//            // keySet 移除 2 所以讀不到了
//            s = testhm.get(2);
//            Log.v("chiyu", s);
//        }catch (NullPointerException e){
//            Log.v("chiyu","there is a null pointer");
//        }
//
//        // 複寫測試
//        Log.v("chiyu",testhm.get(3));
//        testhm.put(3,"dog");
//        Log.v("chiyu",testhm.get(3));
        }

    }

    class piece {
        // 棋子大小
        private byte size;
        // 棋子陣營
        private byte faction;
        // 棋子編號
        private int pieceID;

        // 建構式
        piece(byte size,byte faction,int ID){
            this.size = size;
            this.faction = faction;
            this.pieceID = ID;
        }

        // 取得棋子資料
        // 個別資料
        byte getSize() {return size;}
        byte getFaction() {return faction;}
        // 棋子資料 總和數字化 紀錄棋譜用
        // (ＩＤ 4bit) (大小 4bit) (陣營 4bit)
        int getPieceInfo() {
            return (pieceID)*256 + (((int)size)*16) + (int)faction;
        }



    }

    class player {
        // 陣營
        private byte faction;

        // 玩家編號
        private int playerID;

        // 玩家名稱
        private String name;

        // 棋庫(手上的棋子)
        private HashMap<Byte,Integer> piecesStore;

        // Constructor
        player(int ID, byte faction, int ration, String name) {
            this.playerID = ID;
            this.faction = faction;
            this.name = name;
            piecesStore = new HashMap<>();
            // 配給棋子
            // 0 是無效棋子 所以 counter 從 1 開始
            for(byte counter = 1 ; counter < GameCore.sizeName.length ; counter++) {
                piecesStore.put(counter,ration);
            }
        }

        // 創造新棋
        piece createNewPiece(byte size) {
            // TODO 目前 棋子ＩＤ 沒有用
            int pID=1;
            int remain;
            // 讀取剩餘棋
            remain = piecesStore.get(size);
            // 檢查還有沒有剩餘
            if(remain > 0) {
                // 減少剩餘棋
                piecesStore.put(size,(remain-1));
                // 回傳棋子
                return new piece(size,faction,pID);
            } else {
                EList.add(ErrorCode.pieceRunOut);
                return new piece((byte) 0 ,(byte) 0, 0);
            }
        }

        // 查詢每種棋子剩餘
        int amountInStore(byte size) {return piecesStore.get(GameCore.sizeName[size]);}

        int getPlayerID() {return playerID;}
        byte getFaction() {return faction;}
        String getName() {return name;}

        //不玩了 投降
        private void surrender() {}
    }

    class gameReferee {
        // 紀錄成為 gamePoint 的棋格
        HashSet<Integer> gamePointGrids;

        // 棋盤狀態
        // 因為有暫存棋格0 所以要加一
        private byte[] boardFaction = new byte[GameCore.BoardArea + 1 ];
        private byte[] boardOccupySize = new byte[GameCore.BoardArea + 1 ];
        private byte[] gridStock = new byte[GameCore.BoardArea + 1 ];

        // 初始化
        private void readBoard(checkerboard nowBoard) {
            for(int counter = 0; counter <= (GameCore.BoardArea) ; counter++) {
                boardFaction[counter] = nowBoard.getFaction(counter);
                boardOccupySize[counter] = nowBoard.getSize(counter);
                gridStock[counter] = (byte) nowBoard.boardGrids.get(counter).size();
            }
            gamePointGrids = new HashSet<>();
        }

        // 針對兩個變更棋格去做判斷輸贏
        // 回傳值定義
        // -1 = 出現勝方
        // 其他 = game point 的棋格數
        private int judge(checkerboard board) {
            // 要先讀取 棋盤狀態
            readBoard(board);
            // 重設 gamePointGrids 並塞入墊底的數字
            gamePointGrids.clear();gamePointGrids.add(0);gamePointGrids.add(-1);
            // 本回合有變動的棋格
            //Log.v("chiyu","judge grid" + board.gameRecode.getFirst().target); // trial run
            if(isGameWin((int)board.gameRecode.getLast().get("target"))) return -1;
            //Log.v("chiyu","judge grid" + board.gameRecode.getFirst().destination); // trial run
            if(isGameWin((int)board.gameRecode.getLast().get("destination"))) return -1;
            // 移除墊底的數字
            gamePointGrids.remove(0);gamePointGrids.remove(-1);
            return gamePointGrids.size();
        }

        // 判斷是否有 check point
        private boolean isGameWin(int grid){
            // 暫存格 不做判斷
            if(grid == 0) return false;
            // 暫存 gamePoint Grid 用的
            int notSameGrid;
            // 棋子所在座標
            int column = (grid - 1) / GameCore.BoardWidth;
            int row = ((grid - 1) % GameCore.BoardWidth) + 1;

            // 先做橫向判斷
            notSameGrid = 0;
            for(int counter = 1;counter < GameCore.BoardWidth;counter++) {
                int temp = (((row - 1 + counter) % GameCore.BoardWidth) + 1) + (column * GameCore.BoardWidth);
                //Log.v("chiyu","compare grid : " + temp); // trial run
                // 當出現與當前棋格 不同陣營的 就進入
                if(boardFaction[grid] != boardFaction[temp]) {
                    // 第一次遇到非同陣營的格子 就存起來
                    if(notSameGrid == 0) notSameGrid = temp;
                    // 第二次遇到非同陣營的格子
                    // 將 notSameGrid 設為 -1 讓下面的勝負判斷不會出錯 並跳出
                    else {notSameGrid = -1; break;}
                }
            }
            // 全部都一樣 勝利
            if(notSameGrid == 0 && boardFaction[grid] != 0) {
                message = playerList.get((int)boardFaction[grid]).getName() + " is Winner!!!";
                return true;
            }
            // 有不一樣的 就是 gamePoint
            gamePointGrids.add(notSameGrid);

            // 做直向判斷
            notSameGrid = 0;
            for(int counter = 1;counter < GameCore.BoardWidth;counter++) {
                int temp = row + (((column + counter) % GameCore.BoardHeight) * GameCore.BoardWidth);
                //Log.v("chiyu","compare grid : " + temp); // trial run
                // 當出現與當前棋格 不同陣營的 就進入
                if(boardFaction[grid] != boardFaction[temp]) {
                    // 第一次遇到非同陣營的格子 就存起來
                    if(notSameGrid == 0) notSameGrid = temp;
                        // 第二次遇到非同陣營的格子
                        // 將 notSameGrid 設為 -1 讓下面的勝負判斷不會出錯 並跳出
                    else {notSameGrid = -1; break;}
                }
            }
            // 全部都一樣 勝利
            if(notSameGrid == 0 && boardFaction[grid] != 0) {
                // Log.v("chiyu","" + playerList.get((int)boardFaction[grid]).getName());
                message = playerList.get((int)boardFaction[grid]).getName() + " is Winner!!!";
                return true;
            }
            // 有不一樣的 就是 gamePoint
            gamePointGrids.add(notSameGrid);

            // 先做斜向判斷 (\\\左上右下)
            if((row - 1) == column) {
                notSameGrid = 0;
                for (int counter = 1; counter < GameCore.BoardWidth; counter++) {
                    int temp = (((row - 1 + counter) % GameCore.BoardWidth) + 1)
                            + (( (column + counter) % GameCore.BoardHeight ) * GameCore.BoardWidth);
                    //Log.v("chiyu","compare grid : " + temp); // trial run
                    // 當出現與當前棋格 不同陣營的 就進入
                    if (boardFaction[grid] != boardFaction[temp]) {
                        // 第一次遇到非同陣營的格子 就存起來
                        if (notSameGrid == 0) notSameGrid = temp;
                            // 第二次遇到非同陣營的格子
                            // 將 notSameGrid 設為 -1 讓下面的勝負判斷不會出錯 並跳出
                        else {
                            notSameGrid = -1;
                            break;
                        }
                    }
                }
                // 全部都一樣 勝利
                if(notSameGrid == 0 && boardFaction[grid] != 0) {
                    message = playerList.get((int)boardFaction[grid]).getName() + " is Winner!!!";
                    return true;
                }
                // 有不一樣的 就是 gamePoint
                gamePointGrids.add(notSameGrid);
            }

            // 做斜向判斷 (///右上左下)
            if((row + column) == GameCore.BoardWidth)  {
                notSameGrid = 0;
                for (int counter = 1; counter < GameCore.BoardWidth; counter++) {
                    int Tcolumn = (column + counter) % GameCore.BoardWidth;
                    int Trow = GameCore.BoardWidth - Tcolumn;
                    int temp = Tcolumn * GameCore.BoardWidth + Trow;
                    //Log.v("chiyu","compare grid : " + temp); // trial run
                    // 當出現與當前棋格 不同陣營的 就進入
                    if (boardFaction[grid] != boardFaction[temp]) {
                        // 第一次遇到非同陣營的格子 就存起來
                        if (notSameGrid == 0) notSameGrid = temp;
                            // 第二次遇到非同陣營的格子
                            // 將 notSameGrid 設為 -1 讓下面的勝負判斷不會出錯 並跳出
                        else {
                            notSameGrid = -1;
                            break;
                        }
                    }
                }
                // 全部都一樣 勝利
                if(notSameGrid == 0 && boardFaction[grid] != 0) {
                    message += playerList.get((int)boardFaction[grid]).getName() + " is Winner!!!";
                    return true;
                }
                // 有不一樣的 就是 gamePoint
                gamePointGrids.add(notSameGrid);
            }
            return false;
        }

        // 判斷重複步 之後做
        public void isRepeat() {}

        // 這段是測試程式 可以把棋盤狀態輸出到 log
        public void RefereeTest()  {
            Log.v("chiyu","Now State : " + nowState);
            Log.v("chiyu","nowPlayerID : "+nowPlayerID);
            Log.v("chiyu","grid  faction  Size   stock");
            for(int i=0;i<boardOccupySize.length;i++) {
                Log.v("chiyu",i + " : " + boardFaction[i] + " : " + boardOccupySize[i] + " : " + gridStock[i]);
            }
        }
    }

    // 錯誤碼
    public HashSet<Integer> EList = new HashSet<>();
    final class ErrorCode {
        // 想要放入墊底的無效棋
        static final int depositVoidPiece = 101;
        // 拿到墊底的無效棋
        static final int withdrawVoidPiece = 102;
        // 蓋到錯誤大小的棋子
        static final int sizeErrorPiece = 103;
        // 玩家創建新棋子 卻發現手上沒有剩餘棋子了
        static final int pieceRunOut = 104;
        // 不是自己的回合 還要操作
        static final int nowPlayerIDNotMatch = 105;
        // 拿到不是該回合的棋子
        static final int pieceWithErrorFaction = 106;
        // 想要把棋子放到暫存格
        static final int pieceMoveIntoGridZero = 107;
        // 做了不是當下 state 應該做的事
        static final int stateError = 108;
        // 在棋局進行到一半 想要變更目前的玩家回合
        static final int changeNowPlayerIDwhilePlaying = 109;
    }

    }
