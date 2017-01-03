package tw.org.iii.stickygobblet_v4;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */

public class GameLink {
    FirebaseDatabase database;
    DatabaseReference gameRef;

    private String myName;


    public GameLink(String myName) {
        database = FirebaseDatabase.getInstance();
        gameRef = database.getReference("StickyGobblet");
        this.myName = myName;
        // 驗證連線
        gameRef.child("LinkTest").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue().toString().equals("OK")) connect = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        createRoom();
    }

    // 檢測連線狀態
    boolean connect;
    boolean isConnect() {return connect;}

    // 開新房間
    String key;
    public void createRoom(){
        key = gameRef.child("Waiting").push().getKey();
        gameRef.child("/Waiting/" + key + "/host/").setValue(myName);
    }

    //
    public void uploadPlayingGame(GameOnNet gameOnNet) {
        gameRef.child("/Playing/" + key).setValue(gameOnNet);
    }

}


class GameOnNet {
    int gameState;
    int nowPlayer;
    LinkedList<HashMap<String,Integer>> gameRecode;

    GameOnNet(int gameState, int nowPlayer, LinkedList gameRecode) {
        this.gameState = gameState;
        this.nowPlayer = nowPlayer;
        this.gameRecode = gameRecode;
    }

    GameOnNet() {}
}