package tw.org.iii.stickygobblet_v4;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;

/**
 * This Activity is Game's View & Control part
 * Core is in "GameCore" class
 */
public class GameActivity extends AppCompatActivity {
    TextView tv;

    // 遊戲引擎
    GameCore gameCore;
    // 每種棋子分配的數量
    static final int PieceRation = 2;
    // 陣營 單機版就是both   host(房主)是blue  client(房客)是orange
    String[] Faction = {null,"blue","orange","both"};
    byte myFaction;
    // 紀錄玩家名稱
    String[] playersName = {null,"BLUE","ORANGE"}; // 所有玩家的
    String player; // 你的名字！！！
    // 因為 view 這裡是用 blue orange 寫的～～～ 只好弄個對應表出來 一開始沒想好 哈哈
    HashMap<String,String> playersList;

    //Boolean[] openGrid = {null,true,false,true,false,true,false,true,false,true}; // Trail Run

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        tv = (TextView) findViewById(R.id.tv);
        playersList = new HashMap<>();

        // 讀取遊戲模式
        Bundle bundle = getIntent().getExtras();
        myFaction = (byte) bundle.getInt("Mode");
        String key = bundle.getString("key");
        playersName[1] = bundle.getString("name1");
        playersName[2] = bundle.getString("name2");

        // 進行連線
        initNetwork(this,myFaction,key);
        initView();

        // 單機版就直接進入遊戲
        if(myFaction == 3) playersArrived();

    }

    // 啟動核心
    void initCore() {
        gameCore = new GameCore();
        // 起動引擎
        gameCore.GO();
        // 加入玩家
        gameCore.addPlayer(playersName[1]);
        gameCore.addPlayer(playersName[2]);
        // 遊戲開始 並取得目前是誰的回合
        String result = gameCore.GO();
        // 單機版的 就自己顯示
        if(myFaction == 3)
            tv.setText("Game Start!!!\n" + result + "'s turn!");
    }

// ============== 聯網對戰 ================

    // 當玩家到齊 會呼叫這個方法
    // 啟動遊戲核心與控制介面
    void playersArrived() {
        playersList.put("BLUE", playersName[1]);
        playersList.put("ORANGE", playersName[2]);

        // 這裡要設定 棋子才可以動喔
        if(myFaction == 1) player = playersName[1];
        else if(myFaction == 2) player = playersName[2];
        // 啟動遊戲核心與控制介面
        initControl();
        initCore();
    }

    GameLink gameLink;
    void initNetwork(Context context, int Mode, String key) {
        if(Mode != 2)
            gameLink = new GameLink(context, playersName[1]);
        else {
            gameLink = new GameLink(context, playersName[2], key);
        }
    }

    void netControlGame(String faction, Integer fromGrid, Integer toGrid, int size) {
        // >>>>> Control <<<<<
        String netPlayer = playersList.get(faction);
        if(fromGrid == 0){
            // 從棋庫拿子
            gameCore.playerMove(netPlayer,fromGrid,toGrid,(byte) size);
        } else {
            // 移動棋子
            gameCore.playerMove(netPlayer,fromGrid,toGrid);
        }



        // >>>>> View <<<<<
        RelativeLayout BG = (RelativeLayout) findViewById(R.id.BoardGrids);
        ViewGroup oldParent = (ViewGroup) BG.getChildAt(0)
                , newParent = (ViewGroup) BG.getChildAt(0);
        PieceStorage pieceStorage;
        if(fromGrid == 0){
            if(faction.equals("BLUE"))
                pieceStorage = bluePS;
            else if(faction.equals("ORANGE"))
                pieceStorage = orangePS;
            // 出錯就 return 掉
            else {pieceStorage = new PieceStorage(null,null); return;}

            switch (size){
                case 1:
                    oldParent = (ViewGroup) pieceStorage.smallPiece;
                    break;
                case 2:
                    oldParent = (ViewGroup) pieceStorage.middlePiece;
                    break;
                case 3:
                    oldParent = (ViewGroup) pieceStorage.bigPiece;
                    break;
            }

            for(int i = 0; i < BG.getChildCount(); i++) {
                if(BG.getChildAt(i).getTag().equals(toGrid.toString())) {
                    newParent = (ViewGroup) BG.getChildAt(i);
                    break;
                }
            }
        } else {
            for(int i = 0; i < BG.getChildCount(); i++) {
                if(BG.getChildAt(i).getTag().equals(toGrid.toString()))
                    newParent = (ViewGroup) BG.getChildAt(i);
                else if(BG.getChildAt(i).getTag().equals(fromGrid.toString()))
                    oldParent = (ViewGroup) BG.getChildAt(i);
            }
        }

        pieceMoving(oldParent,newParent,oldParent.getChildAt(oldParent.getChildCount()-1));
    }


// =============== View ======================
// 這段 code 是從小包到大 從一個棋子處理 再到一組棋子 再放到畫面中

    // 輸入一個 bitmap 包裝成 View
    // 給棋子用的
    class PiecePic extends View {

        Bitmap pic;
        public PiecePic(Context context, Bitmap bitmap) {
            super(context);
            Matrix m1 = new Matrix();

            pic = bitmap;

            // 調整成 80dp
            m1.postScale(80 / (pic.getHeight() / getDensity(context)),80 / (pic.getWidth() / getDensity(context)));
            pic = pic.createBitmap(pic,0,0,pic.getWidth(),pic.getHeight(),m1,false);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            // Log.d("chiyu","onMeasure" + pic.getWidth() + " : " + pic.getHeight());
            setMeasuredDimension(pic.getWidth(),pic.getHeight());
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap(pic,0,0,null);
        }


        public float getDensity(Context context){
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return metrics.density;
        }

    }

    // 一組棋子
    class PieceStorage extends LinearLayout {
        Resources res;
        RelativeLayout smallPiece, middlePiece, bigPiece;
        public PieceStorage(Context context, String Tag) {
            super(context);
            // 水平
            setOrientation(LinearLayout.HORIZONTAL);

            // 抓出棋子的圖片
            res = context.getResources();
            Bitmap small, middle, big;
            if(Tag.equals("BLUE")) {
                small = BitmapFactory.decodeResource(res, R.drawable.blue_1);
                middle = BitmapFactory.decodeResource(res, R.drawable.blue_2);
                big = BitmapFactory.decodeResource(res, R.drawable.blue_3);
            } else
            if(Tag.equals("ORANGE")) {
                small = BitmapFactory.decodeResource(res, R.drawable.orange_1);
                middle = BitmapFactory.decodeResource(res, R.drawable.orange_2);
                big = BitmapFactory.decodeResource(res, R.drawable.orange_3);
            } else {
                small = BitmapFactory.decodeResource(res, R.drawable.error_piece);
                middle = BitmapFactory.decodeResource(res, R.drawable.error_piece);
                big = BitmapFactory.decodeResource(res, R.drawable.error_piece);
            }

            // 弄出 RelativeLayout 放各種棋子
            // 每個棋子有一個 RelativeLayout
            smallPiece = new RelativeLayout(context);
            middlePiece = new RelativeLayout(context);
            bigPiece = new RelativeLayout(context);
            // 判斷棋子 從棋庫拿出來
            smallPiece.setTag("0");middlePiece.setTag("0");bigPiece.setTag("0");

            // 把棋子放進 RelativeLayout
            for(int i = 1; i <= PieceRation; i++) {
                // TODO 這邊有點不知道 是不是該用 context
                PiecePic b1 = new PiecePic(context, small);
                PiecePic b2 = new PiecePic(context, middle);
                PiecePic b3 = new PiecePic(context, big);
                // 讓 ClipData 不會當掉
                b1.setTag(Tag + "1");
                b2.setTag(Tag + "2");
                b3.setTag(Tag + "3");
                // 加入圖片
                smallPiece.addView(b1);
                middlePiece.addView(b2);
                bigPiece.addView(b3);
            }

            // 再把 RelativeLayout 放到 PieceStorage
            addView(smallPiece);addView(middlePiece);addView(bigPiece);

            // 最後 做好的 PieceStorage 會在 initView 被放到畫面中
        }
    }

    PieceStorage bluePS, orangePS;
    void initView() {
        // 創造玩家出棋子的地方 (玩家棋庫)
        LinearLayout PS = (LinearLayout) findViewById(R.id.PieceStorage);


        bluePS = new PieceStorage(this, "BLUE");
        orangePS = new PieceStorage(this, "ORANGE");
        // 判斷自己的陣營 對方陣贏就看不到了
        if(myFaction == 1)
            orangePS.setVisibility(View.GONE);
        else if(myFaction == 2)
            bluePS.setVisibility(View.GONE);

        PS.addView(bluePS);
        PS.addView(orangePS);

    }

    // 處理棋子移動
    void pieceMoving(ViewGroup oldParent, ViewGroup newParent, View pieceView) {
        // 從舊的地方搬出來
        oldParent.removeView(pieceView);

        // 若棋格下方有被蓋著的棋子 就顯示出來
        if(!oldParent.getTag().equals("0")){
            if(oldParent.getChildCount() != 0)
                oldParent.getChildAt(oldParent.getChildCount()-1).setVisibility(View.VISIBLE);
        }

        // 有東西被蓋住 就把下面的變成隱藏
        if(newParent.getChildCount() != 0)
            newParent.getChildAt(newParent.getChildCount()-1).setVisibility(View.INVISIBLE);

        // 把棋子放上去
        newParent.addView(pieceView);
    }

// =============== Control ======================

    class chiyuTouchListener implements View.OnTouchListener{

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){

                ClipData.Item item = new ClipData.Item((CharSequence)view.getTag());
                String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData clipData = new ClipData(view.getTag().toString(),mimeTypes,item);

                // 移動的影子
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);
                //chiyuShadowBiulder myShadow = new chiyuShadowBiulder(view);


                // view.startDrag(clipData,myShadow,null,0);
                // 第三個參數是為了要給 OnDragListener 處理 DROP 事件
                // null 會抓不到喔
                view.startDrag(clipData,myShadow,view,0);
            }
            return false;
        }
    }

    class chiyuDragListener implements View.OnDragListener{

        @Override
        public boolean onDrag(View v, DragEvent event) {

            String area = v.getTag().toString();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    //Log.d("chiyu","ACTION_DRAG_STARTED: " + area  + "\n");
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d("chiyu","ACTION_DRAG_ENTERED: " + area  + "\n");
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    Log.d("chiyu","ACTION_DRAG_EXITED: " + area  + "\n");
                    break;
                case DragEvent.ACTION_DROP:
                    Log.d("chiyu","ACTION_DROP: " + area  + "\n");
                    // 抓出移動的那個View(棋子)
                    View piece = (View)event.getLocalState();
                    // 抓出棋子的新舊住址
                    ViewGroup oldParent = (ViewGroup)piece.getParent();
                    ViewGroup newParent = (ViewGroup)v;

                    Log.d("chiyu","棋子 "+ piece.getTag() + ",from " + oldParent.getTag() + ",to " + v.getTag()); // Trail Run

                    // 如果不是單機版 就要檢查目前的連線遊戲狀態
                    // 若不是玩家回合 就不能操作
                    if(myFaction != 3 &&
                            gameLink.downloadGameOnNet.gameState != GameOnNet.GameState.waitPlayer)
                        return false;

                    //if(!openGrid[Integer.parseInt(v.getTag().toString())]) return false; // Trail Run 沒有核心時用的
                    // 核心處理移動 不可以就回傳 false
                    if(!PMove(piece.getTag().toString(),
                                    oldParent.getTag().toString(),
                                    v.getTag().toString())) return false;

                    int GameState = GameOnNet.GameState.gameSync;
                    // 判斷是否分出勝負了
                    String result = gameCore.GO();
                    if(result.equals("905")) {  // 905 --> GameCore.state_End
                        GameState = GameOnNet.GameState.gameOver;
                        tv.setText(gameCore.message);
                    } else {
                        tv.setText(result + "'s turn!");
                    }
                    //tv.setText(gameCore.GO() + "'s turn!");




                    // TODO 上傳遊戲狀況
                    if(gameLink.isConnect())
                        gameLink.uploadPlayingGame(
                                new GameOnNet(GameState,
                                        gameCore.getNowPlayerID(),
                                        gameCore.gameCheckerBoard.gameRecode));


                    // 移動棋子(畫面上)
                    pieceMoving(oldParent,newParent,piece);
//                    // 從舊的地方搬出來
//                    oldParent.removeView(piece);
//
//                    // 若棋格下方有被蓋著的棋子 就顯示出來
//                    if(!oldParent.getTag().equals("0")){
//                        if(oldParent.getChildCount() != 0)
//                            oldParent.getChildAt(oldParent.getChildCount()-1).setVisibility(View.VISIBLE);
//                    }
//
//                    // 有東西被蓋住 就把下面的變成隱藏
//                    if(newParent.getChildCount() != 0)
//                        newParent.getChildAt(newParent.getChildCount()-1).setVisibility(View.INVISIBLE);
//
//                    // 把棋子放上去
//                    newParent.addView(piece);

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    // Log.d("chiyu","ACTION_DRAG_ENDED: " + area  + "\n");
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    class chiyuShadowBiulder extends View.DragShadowBuilder {
        private Drawable shadow1;
        View nowView;

        public chiyuShadowBiulder(View v){
            super(v);
            nowView = v;
            shadow1 = new ColorDrawable(Color.LTGRAY);
        }

        @Override
        public void onProvideShadowMetrics(Point outShadowSize, Point outShadowTouchPoint) {
            super.onProvideShadowMetrics(outShadowSize, outShadowTouchPoint);
            int width, height;
            width = nowView.getWidth();
            //width = getView().getWidth()/2;
            height = nowView.getHeight();
            //height = getView().getHeight()/2;
            Log.d("chiyu","DragShadowBuilder width : " + width + ",height : " + height + ",tag : " + nowView.getTag());

            shadow1.setBounds(0,0,width,height);
            outShadowSize.set(width,height);
            //outShadowTouchPoint.set(width,height);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            super.onDrawShadow(canvas);
            shadow1.draw(canvas);
        }
    }

    void initControl() {
        // 處理玩家棋庫
        // 把所有棋子 都綁上拖曳的功能
        chiyuTouchListener PSClick = new chiyuTouchListener();
        if(bluePS != null) {
            addListener2Child(bluePS.smallPiece,PSClick);
            addListener2Child(bluePS.middlePiece,PSClick);
            addListener2Child(bluePS.bigPiece,PSClick);
        }
        if(orangePS != null) {
            addListener2Child(orangePS.smallPiece,PSClick);
            addListener2Child(orangePS.middlePiece,PSClick);
            addListener2Child(orangePS.bigPiece,PSClick);
        }

        // 處理棋格
        // 所有棋格都綁上"放東西進來"的功能
        chiyuDragListener GridDrag = new chiyuDragListener();
        RelativeLayout BG = (RelativeLayout) findViewById(R.id.BoardGrids);
        addListener2Child(BG,GridDrag);
    }

    // 把一個 viewGroup 裡面子元素 都綁上同一個動作 listener 用的小副函式
    // 因為只有兩個類型的 listener 所以用 try catch 處理
    // 出現第三種就掛啦
    void addListener2Child(ViewGroup viewGroup,Object listener) {
        for(int i = 0; i < viewGroup.getChildCount(); i++) {
            try {
                viewGroup.getChildAt(i).setOnDragListener((View.OnDragListener) listener);
            }catch (ClassCastException e) {
                viewGroup.getChildAt(i).setOnTouchListener((View.OnTouchListener) listener);
            }
        }
    }

    boolean PMove(String piece, String from, String to) {
        int fromGrid, toGrid, size;
        String s,e;

        s = piece.substring(0,1);
        e = piece.substring(piece.length()-1);
        // Log.d("chiyu","s " + s + " e " + e);

        fromGrid = Integer.parseInt(from);
        toGrid = Integer.parseInt(to);
        size = Integer.parseInt(e);

        // 單機版才用到 判斷你拿的棋子是哪邊的
        // 因為網路版 不能變更自己的名字
        if(myFaction == 3) player = playersList.get(s.equals("O")? "ORANGE" : "BLUE");

        if(fromGrid == 0){
            // 從棋庫拿子
            return gameCore.playerMove(player,fromGrid,toGrid,(byte) size);
        } else {
            // 移動棋子
            return gameCore.playerMove(player,fromGrid,toGrid);
        }

    }





}
