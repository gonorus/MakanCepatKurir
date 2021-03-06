package gonorus.makancepatkurir.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import gonorus.makancepatkurir.R;
import gonorus.makancepatkurir.adapter.ItemDrawerAdapter;
import gonorus.makancepatkurir.customutil.SessionManager;
import gonorus.makancepatkurir.model.ModelKurir;
import gonorus.makancepatkurir.rest.Communicator;
import gonorus.makancepatkurir.rest.LoginInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityHome extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce = false;

    private Toolbar toolbar;
    private ImageView notificationIcon;
    private DrawerLayout drawer;
    private ItemDrawerAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    private NavigationView navigationView;
    private TextView txtTitleBar;
    private CircleImageView profileImage;
    private boolean menuClickedOrNot = false;
    private SessionManager sessionManager;
    private int temp;
    private Intent intentTransaksi = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();

        try {
            temp = getIntent().getIntExtra("notifikasiAlarm", 0);
        } catch (Exception E) {
            temp = 0;
        }
        selectItem(temp);
    }

    private void init() {
        sessionManager = new SessionManager(this);
        intentTransaksi = new Intent(this, ActivityTransaction.class);

        toolbar = (Toolbar) findViewById(R.id.toolbar_home);
        txtTitleBar = (TextView) findViewById(R.id.txtTitleAppBar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        expListView = (ExpandableListView) findViewById(R.id.drawerlist);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        notificationIcon = (ImageView) findViewById(R.id.btnNotifAppBar);

        prepareListData();
        initView();
    }

    private void initView() {
        if (sessionManager.getKurirDetails().get(SessionManager.KEY_IS_VALIDATE).equals("0")) {
            notificationIcon.setVisibility(View.GONE);
        }

        /*
        * SETTING APPBAR
        * */
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(3);
                expListView.setItemChecked(0, false);
            }
        });
        setMyTitleBar("Makan Cepat");

        /*
        * SETTING DRAWER
        * */
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        listAdapter = new ItemDrawerAdapter(this, listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);
        expListView.setItemChecked(0, true);
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                if (groupPosition != 11) {
                    int index = expandableListView.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(groupPosition));
                    expandableListView.setItemChecked(index, true);
                    selectItem(groupPosition);
                    menuClickedOrNot = true;
                }
                return menuClickedOrNot;
            }
        });

        /*
        * SETTING HEADER DRAWER
        * */
        View headerview = navigationView.getHeaderView(0);
        profileImage = (CircleImageView) headerview.findViewById(R.id.profile_image);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityHome.this, ActivityDetailProfile.class);
                startActivity(intent);
            }
        });
        TextView txtUsername = (TextView) headerview.findViewById(R.id.nav_header_txt_username);
        txtUsername.setText(sessionManager.getKurirDetails().get(SessionManager.KEY_NAME));
        try {
            if (!(sessionManager.getKurirDetails().get(SessionManager.KEY_FOTO)).trim().isEmpty())
                Picasso.with(getApplicationContext()).load(Communicator.BASE_URL + "kurir/images/profile/" + sessionManager.getKurirDetails().get(SessionManager.KEY_FOTO)).skipMemoryCache().into(profileImage);
        } catch (NullPointerException NPE) {
            Log.e("MAKANCEPAT", NPE.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (temp == 3) {
            selectItem(4);
        } else if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //Checking for fragment count on backstack
            if (!doubleBackToExitPressedOnce) {
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Please click BACK again to exit.", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.menuHomeCart) {
            Intent intent = new Intent(getApplicationContext(), ActivitySearchResult.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuHomeSearch) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /***********************************************************************************************
     * ACTIVITY METHOD CONTROL
     ***********************************************************************************************/
    /*
    * Set Title Activity
    * */
    public void setMyTitleBar(String title) {
        txtTitleBar.setText(title);
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<>();

        // Adding Header data
        listDataHeader.add(0, "Halaman Depan");
        listDataHeader.add(1, "Transaksi");
        listDataHeader.add(2, "Logout");
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        temp = position;
        // Create a new fragment and specify the planet to show based on position
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment;

        switch (position) {
            case 0:
                fragment = new FragmentHalamanDepan();
                fragmentTransaction
                        .replace(R.id.content_frame, fragment)
                        .commit();
                drawer.closeDrawer(GravityCompat.START);
                break;
            case 1:
                if (!sessionManager.getKurirDetails().get(SessionManager.KEY_ID_TRANSAKSI).equals("0")) {
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    startActivity(intentTransaksi);
                    finish();
                    expListView.setItemChecked(position, true);
                } else {
                    expListView.setItemChecked(0, true);
                }
                break;
            case 2:
                showLogoutDialog();
                break;
            case 3:
                fragmentTransaction.setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left);
                fragment = new FragmentNotification();
                fragmentTransaction
                        .replace(R.id.content_frame, fragment)
                        .commit();
                drawer.closeDrawer(GravityCompat.START);
                break;
            case 4:
                fragmentTransaction.setCustomAnimations(R.anim.slide_from_left, R.anim.slide_to_right);
                fragment = new FragmentHalamanDepan();
                fragmentTransaction
                        .replace(R.id.content_frame, fragment)
                        .commit();
                drawer.closeDrawer(GravityCompat.START);
                expListView.setItemChecked(0, true);
                break;
            default:
                fragment = new FragmentHalamanDepan();
                fragmentTransaction
                        .replace(R.id.content_frame, fragment)
                        .commit();
                drawer.closeDrawer(GravityCompat.START);
                break;
        }
    }

    public void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        // Add the buttons
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                LoginInterface apiService = Communicator.getClient().create(LoginInterface.class);
                Call<ModelKurir> user = apiService.checkLogout(
                        sessionManager.getKurirDetails().get(SessionManager.KEY_EMAIL),
                        sessionManager.getKurirDetails().get(SessionManager.KEY_VALIDATE)
                );
                user.enqueue(new Callback<ModelKurir>() {
                    @Override
                    public void onResponse(Call<ModelKurir> call, Response<ModelKurir> response) {
                        // SUCCEED TO LOGOUT

                        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                        finish();
                        sessionManager.logoutUser();
                    }

                    @Override
                    public void onFailure(Call<ModelKurir> call, Throwable t) {
                        // FAILED TO LOGOUT
                    }
                });
            }
        });
        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                expListView.setItemChecked(0, true);
            }
        });

        builder.setMessage("Yakin ingin keluar?").setTitle("Konfirmasi");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
        intentTransaksi = new Intent(this, ActivityTransaction.class);
        intentTransaksi.putExtra("NotifikasiFirebaseIntent", Integer.parseInt(getIntent().getExtras().get("NotifikasiFirebaseIntent").toString()));
        */

        try {
            temp = getIntent().getIntExtra("notifikasiAlarm", 0);
        } catch (Exception E) {
            temp = 0;
        }
        selectItem(temp);
    }
}
