package tw.org.iii.stickygobblet_v4;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    EditText name1;
    public void Start(View v) {

        Intent ii = new Intent();
        ii.setClass(this, GameActivity.class);

        name1 = (EditText) findViewById(R.id.name1);

        Bundle bundle = new Bundle();
        bundle.putString("name1", name1.getText().toString());
        bundle.putString("name2", "Orange Computer");
        bundle.putInt("Mode", 3);

        ii.putExtras(bundle);
        startActivity(ii);
    }

    public void Online1(View v) {

        Intent ii = new Intent();
        ii.setClass(this, GameActivity.class);

        name1 = (EditText) findViewById(R.id.name1);

        Bundle bundle = new Bundle();
        bundle.putString("name1", name1.getText().toString());
        bundle.putInt("Mode", 1);

        ii.putExtras(bundle);
        startActivity(ii);
    }

    public void Online2(View v) {
        GameLink gl = new GameLink("TESTMAN");

    }



}
