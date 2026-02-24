package com.example.gamecenter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.gamecenter.GameSelectorActivity;
import com.example.gamecenter.R;
import com.example.gamecenter.database.GameCenterOpenHelper;

public class LoginActivity extends AppCompatActivity {

    public static final String SESSION_PREFS = "SESSION_PREFS";
    public static final String KEY_USER_ID = "USER_ID";
    public static final String KEY_USERNAME = "USERNAME";

    private GameCenterOpenHelper database;

    private EditText usernameEt;
    private EditText passwordEt;
    private Button loginBtn;
    private Button registerBtn;
    private TextView statusTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Screen config
        setContentView(R.layout.app_activity_login);

        // Hide system bars (Status Bar & Navigation Bar)
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        controller.hide(WindowInsetsCompat.Type.systemBars());

        database = new GameCenterOpenHelper(this);

        bindViews();
        bindActions();
    }

    private void bindViews() {
        usernameEt = findViewById(R.id.usernameEt);
        passwordEt = findViewById(R.id.passwordEt);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);
        statusTv = findViewById(R.id.statusTv);
    }

    private void bindActions() {
        loginBtn.setOnClickListener(v -> attemptLogin());
        registerBtn.setOnClickListener(v -> attemptRegister());
    }

    private void attemptLogin() {
        String username = getUsernameInput();
        String password = getPasswordInput();

        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Rellena usuario y contraseña");
            return;
        }

        int userId = database.loginUser(username, password);
        if (userId == -1) {
            setStatus("Usuario o contraseña incorrectos");
            Toast.makeText(this, "Login incorrecto", Toast.LENGTH_SHORT).show();
            return;
        }

        saveSession(userId, username);
        setStatus("Login correcto");
        Toast.makeText(this, "Bienvenido, " + username, Toast.LENGTH_SHORT).show();

        goToMainMenu();
    }

    private void attemptRegister() {
        String username = getUsernameInput();
        String password = getPasswordInput();

        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Rellena usuario y contraseña");
            return;
        }

        try {
            long newId = database.registerUser(username, password);
            if (newId <= 0) {
                setStatus("No se pudo registrar (error desconocido)");
                Toast.makeText(this, "Registro fallido", Toast.LENGTH_SHORT).show();
                return;
            }

            // Después de registrar, logueamos automáticamente
            saveSession((int) newId, username);
            setStatus("Registrado y logueado");
            Toast.makeText(this, "Usuario creado: " + username, Toast.LENGTH_SHORT).show();

            goToMainMenu();

        } catch (IllegalStateException e) {
            // registerUser lanza esto si el usuario ya existe (según lo definimos)
            setStatus("Ese usuario ya existe");
            Toast.makeText(this, "Ese usuario ya existe", Toast.LENGTH_SHORT).show();

        } catch (IllegalArgumentException e) {
            setStatus("Datos inválidos");
            Toast.makeText(this, "Datos inválidos", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            setStatus("Error registrando: " + e.getMessage());
            Toast.makeText(this, "Error registrando", Toast.LENGTH_SHORT).show();
        }
    }

    private String getUsernameInput() {
        if (usernameEt.getText() == null) return "";
        return usernameEt.getText().toString().trim();
    }

    private String getPasswordInput() {
        if (passwordEt.getText() == null) return "";
        return passwordEt.getText().toString();
    }

    private void setStatus(String msg) {
        statusTv.setText(msg);
    }

    private void saveSession(int userId, String username) {
        SharedPreferences prefs = getSharedPreferences(SESSION_PREFS, MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();
    }

    private void goToMainMenu() {
        Intent intent = new Intent(this, GameSelectorActivity.class);
        startActivity(intent);
        finish();
    }
}