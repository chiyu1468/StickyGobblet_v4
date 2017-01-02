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

    public void Start(View v) {
        Intent ii = new Intent();
        ii.setClass(this, GameActivity.class);

        EditText name1 = (EditText) findViewById(R.id.name1);
        EditText name2 = (EditText) findViewById(R.id.name2);

        Log.d("chiyu",name1.getText().toString());

        Bundle bundle = new Bundle();
        bundle.putString("name1", name1.getText().toString());
        bundle.putString("name2", name2.getText().toString());

        ii.putExtras(bundle);
        startActivity(ii);
    }




}
