package com.example.ecom.Buyers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecom.Model.Cart;
import com.example.ecom.Prevalent.Prevalent;

import com.example.ecom.R;
import com.example.ecom.ViewHolder.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class CartActivity extends AppCompatActivity
{

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button NextProcessBtn;
    private TextView txttotalAmount, txtMsg1;

    private int overTotalPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.cart_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        NextProcessBtn = (Button) findViewById(R.id.next_btn);
        txttotalAmount = (TextView)findViewById(R.id.page_title);
        txtMsg1 = (TextView)findViewById(R.id.msg1);

        NextProcessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                txttotalAmount.setText("Total Price = Rs." + String.valueOf(overTotalPrice));

                Intent intent = new Intent(CartActivity.this, ConfirmFinalOrderActivity.class);
                intent.putExtra("Total Price", String.valueOf(overTotalPrice));
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        CheckOrderState();

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");
        FirebaseRecyclerOptions<Cart> options = new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(cartListRef.child("User View").
                        child(Prevalent.currentOnlineUser.getPhone()).child("Products"), Cart.class).build();

        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder cartViewHolder, int i, @NonNull Cart cart)
            {
                cartViewHolder.txtProductQuantity.setText("Quantity = " + cart.getQuantity());
                cartViewHolder.txtProductPrice.setText("Price = Rs." + cart.getPrice());
                cartViewHolder.txtProductName.setText(cart.getPname());
                int oneTypeProductTPrice = ((Integer.valueOf(cart.getPrice())))*Integer.valueOf(cart.getQuantity());
                overTotalPrice = overTotalPrice + oneTypeProductTPrice;

                cartViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence options[] = new CharSequence[]
                                {
                                    "Edit", "Remove"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options : ");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if(i == 0)
                                {
                                    Intent intent = new Intent(CartActivity.this, ProductDetailsActivity.class);
                                    intent.putExtra("pid", cart.getPid());
                                    startActivity(intent);
                                }
                                if(i == 1)
                                {
                                    cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone())
                                            .child("Products")
                                            .child(cart.getPid())
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if(task.isSuccessful())
                                                    {
                                                        Toast.makeText(CartActivity.this, "Item removed successfully.", Toast.LENGTH_SHORT).show();

                                                        Intent intent = new Intent(CartActivity.this, HomeActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                }
                            }
                        });

                        builder.show();
                    }
                });
            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().from(parent.getContext()).inflate(R.layout.cart_items_layout, parent, false);
                CartViewHolder holder = new CartViewHolder(view);
                return holder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void CheckOrderState()
    {
        DatabaseReference ordersRef;
        ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhone());

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    String shippingState = snapshot.child("state").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    if(shippingState.equals("shipped"))
                    {
                        txttotalAmount.setText("Dear " + userName + "\n order is shipped successfully.");
                        recyclerView.setVisibility(View.GONE);

                        txtMsg1.setVisibility(View.VISIBLE);
                        txtMsg1.setText("Congratulations, your final order has been shipped successfully. Soon you will receive your final order at your doorstep.");
                        NextProcessBtn.setVisibility(View.GONE);

                        Toast.makeText(CartActivity.this, "You can purchase more products, once you receive your first final order.", Toast.LENGTH_SHORT).show();
                    }
                    else if (shippingState.equals("not shipped"))
                    {
                        txttotalAmount.setText("Shipping State = Not Shipped");
                        recyclerView.setVisibility(View.GONE);

                        txtMsg1.setVisibility(View.VISIBLE);
                        NextProcessBtn.setVisibility(View.GONE);

                        Toast.makeText(CartActivity.this, "You can purchase more products, once you receive your first final order.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }
}