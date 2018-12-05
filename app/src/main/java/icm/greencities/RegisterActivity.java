package icm.greencities;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputCountry, inputCity, inputFullName;
    private FirebaseAuth auth;
    private Button btnSignUp, btnLogin;
    private ProgressDialog PD;
    private FirebaseFirestore db;

    @Override    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        PD = new ProgressDialog(this);
        PD.setMessage("Loading...");
        PD.setCancelable(true);
        PD.setCanceledOnTouchOutside(false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        }

        /*
        DocumentReference docRef = db.collection("users").document("caroliinaalbuquerque29@gmail.com");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("document", "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d("no file", "No such document");
                    }
                } else {
                    Log.d("error", "get failed with ", task.getException());
                }
            }
        });
        */

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        btnLogin = (Button) findViewById(R.id.sign_in_button);
        inputCountry = (EditText) findViewById(R.id.country);
        inputCity = (EditText) findViewById(R.id.city);
        inputFullName = (EditText) findViewById(R.id.name);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override            public void onClick(View view) {

                if (checkConnection()) {
                    final String email = inputEmail.getText().toString();
                    final String password = inputPassword.getText().toString();
                    final String country = inputCountry.getText().toString();
                    final String city = inputCity.getText().toString();
                    final String name = inputFullName.getText().toString();

                    try {
                        if (password.length() > 0 && email.length() > 0) {
                            PD.show();
                            auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (!task.isSuccessful()) {
                                                try {
                                                    throw task.getException();
                                                } catch (FirebaseAuthUserCollisionException e) {
                                                    Toast.makeText(
                                                            RegisterActivity.this,
                                                            "Email already in use",
                                                            Toast.LENGTH_LONG).show();
                                                } catch (FirebaseAuthWeakPasswordException e) {
                                                    Toast.makeText(
                                                            RegisterActivity.this,
                                                            "Invalid length (< 6) or invalid characters",
                                                            Toast.LENGTH_LONG).show();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                            PD.dismiss();
                                        }
                                    });
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", name);
                            user.put("country", country);
                            user.put("city", city);
                            db.collection("users").document(email)
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("sucess", "DocumentSnapshot successfully written!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("error", "Error writing document", e);
                                        }
                                    });
                        } else {
                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Fill All Fields",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(
                            RegisterActivity.this,
                            "This app requires an active internet connection!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override            public void onClick(View view) {
                finish();
            }
        });
    }

    private boolean checkConnection() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

}
