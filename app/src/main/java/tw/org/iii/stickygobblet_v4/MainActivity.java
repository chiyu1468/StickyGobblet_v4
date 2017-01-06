package tw.org.iii.stickygobblet_v4;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    private View lobbyview;
    private View drawer;
    private GridView gridView;
    private MyAdapter myAdapter;
    private float drawerWidth;
    private boolean issetX = false;
    EditText name;
    TextView tv;
    FirebaseDatabase database;
    DatabaseReference roomRef;
    ArrayList<String> rooms;
    ArrayList<String> roomname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lobbyview = findViewById(R.id.lobby);
        drawer = findViewById(R.id.drawer);
        ViewTreeObserver observer = drawer.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                drawerWidth = -drawer.getWidth();
                Log.v("chiyu","getWidth() : " + drawerWidth);
                if (!issetX) {
                    drawer.setX(drawerWidth);
                    issetX = true;
                }
            }
        });
        gridView = (GridView)findViewById(R.id.gridView);
        name = (EditText) findViewById(R.id.name);
        tv = (TextView) findViewById(R.id.tv);
        myAdapter = new MyAdapter(this);
        rooms = new ArrayList<>();
        roomname = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        roomRef = database.getReference("StickyGobblet");
        roomRef.child("/Waiting/").addChildEventListener(new MyChildEventListener());
        gridView.setAdapter(myAdapter);
        MyOnClickListener clickListener = new MyOnClickListener();
        lobbyview.setTag("lobbyClick");
        tv.setTag("tvClick");
        lobbyview.setOnClickListener(clickListener);
        tv.setOnClickListener(clickListener);
    }
    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String s = view.getTag().toString();
            switch (s) {
                case "lobbyClick":
                    Log.v("chiyu","lobbyClick");
                    drawerAnimator(true);
                    break;
                case "tvClick":
                    Log.v("chiyu","tvClick");
                    drawerAnimator(false);
                    break;
            }

        }
    }
    private class MyChildEventListener implements ChildEventListener {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            rooms.add(dataSnapshot.getKey());
            roomname.add(dataSnapshot.child("host").getValue(String.class));
            tv.setText(rooms.size()+" rooms waiting here");
            myAdapter.notifyDataSetChanged();
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            rooms.remove(dataSnapshot.getKey());
            roomname.remove(dataSnapshot.child("host").getValue(String.class));
            tv.setText(rooms.size()+" rooms waiting here");
            myAdapter.notifyDataSetChanged();
        }
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
        @Override
        public void onCancelled(DatabaseError databaseError) {}
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
    public void inRoom(String roomNumber) {
        Intent ii = new Intent();
        ii.setClass(this, GameActivity.class);
        if(rooms.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putString("name2", name.getText().toString());
            bundle.putInt("Mode", 2);
            bundle.putString("key", roomNumber);
            ii.putExtras(bundle);
            startActivity(ii);
        }
    }
    private class MyAdapter extends BaseAdapter {
        private Context context;
        MyAdapter(Context context) {
            this.context = context;
        }
        @Override
        public int getCount() {
            int size = rooms.size();
            return size;
        }
        @Override
        public Object getItem(int i) {
            return null;
        }
        @Override
        public long getItemId(int i) {
            return 0;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.layout_item,null);
            }
            ImageView img = (ImageView)view.findViewById(R.id.item_img);
            TextView title = (TextView)view.findViewById(R.id.item_title);
            img.setImageResource(R.drawable.photo);
            title.setText(roomname.get(i));
            view.setTag(rooms.get(i));
            view.setOnClickListener(new gridViewClickListener());
            return view;
        }
    }
    private class gridViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            inRoom(view.getTag().toString());
        }
    }
    public void drawerAnimatorxxx() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(drawer, "x", drawerWidth, drawerWidth);
        AnimatorSet set = new AnimatorSet();
        //Log.v("chiyu","" + drawer.getX());
        if (drawer.getX() == drawerWidth)
            animator = ObjectAnimator.ofFloat(drawer, "x", drawerWidth, 0);
        else if (drawer.getX() == 0)
            animator = ObjectAnimator.ofFloat(drawer, "x", 0, drawerWidth);
        set.playTogether(animator);
        set.setDuration(500);
        set.start();
    }
    public void drawerAnimator(boolean isin) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(drawer, "x", drawerWidth, drawerWidth);
        AnimatorSet set = new AnimatorSet();
        if ( (drawer.getX() == drawerWidth) && !isin )
            animator = ObjectAnimator.ofFloat(drawer, "x", drawerWidth, 0);
        else if ( (drawer.getX() == 0) && isin)
            animator = ObjectAnimator.ofFloat(drawer, "x", 0, drawerWidth);
        else
            return;
        set.playTogether(animator);
        set.setDuration(500);
        set.start();
    }
}