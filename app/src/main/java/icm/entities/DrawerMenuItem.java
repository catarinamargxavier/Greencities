package icm.entities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import icm.greencities.Goals;
import icm.greencities.LoginActivity;
import icm.greencities.ProfileAtivity;
import icm.greencities.R;
import icm.greencities.ResultsMain;
import icm.greencities.StartActivity;

import static android.app.PendingIntent.getActivity;
import static android.support.v4.content.ContextCompat.startActivity;

@Layout(R.layout.drawer_item)
public class DrawerMenuItem {

    public static final int DRAWER_MENU_ITEM_STARTACTIVITY= 1;
    public static final int DRAWER_MENU_ITEM_GOALS = 2;
    public static final int DRAWER_MENU_ITEM_RESULTS = 3;
    public static final int DRAWER_MENU_ITEM_PROFILE = 4;
    public static final int DRAWER_MENU_ITEM_NOTIFICATIONS = 5;
    public static final int DRAWER_MENU_ITEM_LOGOUT = 6;

    private int mMenuPosition;
    private Context mContext;
    private DrawerCallBack mCallBack;

    @View(R.id.itemNameTxt)
    private TextView itemNameTxt;

    @View(R.id.itemIcon)
    private ImageView itemIcon;

    public DrawerMenuItem(Context context, int menuPosition) {
        mContext = context;
        mMenuPosition = menuPosition;
    }

    @Resolve
    private void onResolved() {
        switch (mMenuPosition){
            case DRAWER_MENU_ITEM_PROFILE:
                itemNameTxt.setText("Profile");
                itemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_person_grey_24dp));
                break;
            case DRAWER_MENU_ITEM_STARTACTIVITY:
                itemNameTxt.setText("Start Activity");
                itemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_play_circle_filled_grey_24dp));
                break;
            case DRAWER_MENU_ITEM_GOALS:
                itemNameTxt.setText("Goals");
                itemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_stars_grey_24dp));
                break;
            case DRAWER_MENU_ITEM_RESULTS:
                itemNameTxt.setText("Results");
                itemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_insert_chart_grey_24dp));
                break;
            case DRAWER_MENU_ITEM_NOTIFICATIONS:
                itemNameTxt.setText("Notifications");
                itemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_notifications_grey_24dp));
                break;
            case DRAWER_MENU_ITEM_LOGOUT:
                itemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_exit_to_app_grey_24dp));
                itemNameTxt.setText("Logout");
                break;
        }
    }

    @Click(R.id.mainView)
    private void onMenuItemClick(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        switch (mMenuPosition){
            case DRAWER_MENU_ITEM_PROFILE:
                mContext.startActivity(new Intent(mContext.getApplicationContext(), ProfileAtivity.class));
                break;
            case DRAWER_MENU_ITEM_STARTACTIVITY:
                mContext.startActivity(new Intent(mContext.getApplicationContext(), StartActivity.class));
                break;
            case DRAWER_MENU_ITEM_GOALS:
                mContext.startActivity(new Intent(mContext.getApplicationContext(), Goals.class));
                break;
            case DRAWER_MENU_ITEM_NOTIFICATIONS:
                Toast.makeText(mContext, "Notifications", Toast.LENGTH_SHORT).show();
                if(mCallBack != null)mCallBack.onNotificationsMenuSelected();
                break;
            case DRAWER_MENU_ITEM_RESULTS:
                mContext.startActivity(new Intent(mContext.getApplicationContext(), ResultsMain.class));
                break;
            case DRAWER_MENU_ITEM_LOGOUT:
                auth.signOut();
                FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user == null) {
                            mContext.startActivity(new Intent(mContext.getApplicationContext(), LoginActivity.class));
                            //finish()
                        }
                    }
                };
                break;
        }
    }

    public void setDrawerCallBack(DrawerCallBack callBack) {
        mCallBack = callBack;
    }

    public interface DrawerCallBack{
        void onProfileMenuSelected();
        void onRequestMenuSelected();
        void onGroupsMenuSelected();
        void onMessagesMenuSelected();
        void onNotificationsMenuSelected();
        void onSettingsMenuSelected();
        void onTermsMenuSelected();
        void onLogoutMenuSelected();
    }
}