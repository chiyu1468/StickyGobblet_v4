package tw.org.iii.stickygobblet_v4;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    EditText name;
    TextView tv;

    FirebaseDatabase database;
    DatabaseReference roomRef;
    ArrayList<String> rooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        name = (EditText) findViewById(R.id.name);
        tv = (TextView) findViewById(R.id.tv);

        rooms = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        roomRef = database.getReference("StickyGobblet");

        roomRef.child("/Waiting/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dd : dataSnapshot.getChildren()) {
                    //Log.v("chiyu","" + dd.getKey());
                    rooms.add(dd.getKey());
                    tv.setText(rooms.size()+" rooms waiting here");
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public void Start(View v) {

        Intent ii = new Intent();
        ii.setClass(this, GameActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("name1", name.getText().toString());
        bundle.putString("name2", "Orange Computer");
        bundle.putInt("Mode", 3);

        ii.putExtras(bundle);
        startActivity(ii);
    }

    public void Online1(View v) {

        Intent ii = new Intent();
        ii.setClass(this, GameActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("name1", name.getText().toString());
        bundle.putInt("Mode", 1);

        ii.putExtras(bundle);
        startActivity(ii);
    }

    public void Online2(View v) {

        Intent ii = new Intent();
        ii.setClass(this, GameActivity.class);

        if(rooms.size() > 0) {

            Bundle bundle = new Bundle();
            bundle.putString("name2", name.getText().toString());
            bundle.putInt("Mode", 2);
            bundle.putString("key", rooms.get(rooms.size() - 1));

            ii.putExtras(bundle);
            startActivity(ii);
        }

    }



}
