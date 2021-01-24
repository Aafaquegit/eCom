package com.example.ecom.Sellers;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecom.Admin.AdminCheckNewProductsActivity;
import com.example.ecom.Model.Products;
import com.example.ecom.Sellers.SellerProductCategoryActivity;
import com.example.ecom.Buyers.MainActivity;
import com.example.ecom.R;
import com.example.ecom.Sellers.ui.dashboard.DashboardFragment;
import com.example.ecom.Sellers.ui.home.HomeFragment;
import com.example.ecom.Sellers.ui.notifications.NotificationsFragment;
import com.example.ecom.ViewHolder.ItemViewHolder;
import com.example.ecom.ViewHolder.ProductViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SellerHomeActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private DatabaseReference unverifiedProductsRef;
    private TextView mTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_home);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        unverifiedProductsRef = FirebaseDatabase.getInstance().getReference().child("Products");

        recyclerView = findViewById(R.id.seller_home_recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mTextMessage = findViewById(R.id.message);
        OpenReselector(navView);

//        navView.setOnNavigationItemReselectedListener ((BottomNavigationView.OnNavigationItemReselectedListener) mOnNavigationItemSelectedListener);
    }

    private void OpenReselector(BottomNavigationView navView)
    {
        navView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        Intent intentHome = new Intent(SellerHomeActivity.this, SellerHomeActivity.class);
                        startActivity(intentHome);
                        break;

                    case R.id.navigation_add:
                        Intent intentCate = new Intent(SellerHomeActivity.this, SellerProductCategoryActivity.class);
                        startActivity(intentCate);
                        break;

                    case R.id.navigation_logout:
                        final FirebaseAuth mAuth;
                        mAuth = FirebaseAuth.getInstance();
                        mAuth.signOut();

                        Intent intentMain = new Intent(SellerHomeActivity.this, MainActivity.class);
                        intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intentMain);
                        finish();

                        break;
                }
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Products> options =
                new FirebaseRecyclerOptions.Builder<Products>()
                        .setQuery(unverifiedProductsRef.orderByChild("sid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()), Products.class)
                        .build();

        FirebaseRecyclerAdapter<Products, ItemViewHolder> adapter =
                new FirebaseRecyclerAdapter<Products, ItemViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull ItemViewHolder productViewHolder, int i, @NonNull Products model)
                    {
                        productViewHolder.txtProductName.setText(model.getPname());
                        productViewHolder.txtProductDescription.setText(model.getDescription());
                        productViewHolder.txtProductStatus.setText("State : " + model.getProductState());
                        productViewHolder.txtProductPrice.setText("Price = Rs." + model.getPrice());
                        Picasso.get().load(model.getImage()).into(productViewHolder.imageView);

                        productViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final String productId = model.getPid();

                                CharSequence options[] = new CharSequence[]
                                        {
                                                "Yes",
                                                "No"
                                        };

                                AlertDialog.Builder builder = new AlertDialog.Builder(SellerHomeActivity.this);
                                builder.setTitle("Do you want to Delete this Product. Are you sure?");
                                builder.setItems(options, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int position)
                                    {
                                        if(position == 0)
                                        {
                                            deleteProduct(productId);
                                        }
                                        if(position == 1)
                                        {

                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.seller_item_view, parent, false);
                        ItemViewHolder holder = new ItemViewHolder(view);
                        return holder;
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void deleteProduct(String productId)
    {
        unverifiedProductsRef.child(productId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        Toast.makeText(SellerHomeActivity.this, "That Product has been Deleted Successfully.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /*    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener()
    {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;

                case R.id.navigation_add:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;

                case R.id.navigation_logout:
                    final FirebaseAuth mAuth;
                    mAuth = FirebaseAuth.getInstance();
                    mAuth.signOut();

                    Intent intent = new Intent(SellerHomeActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                    return true;
            }
            return false;
        }
    }; */
}