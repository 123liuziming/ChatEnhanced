package com.rdc.p2p.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.RegisterMutation;
import com.rdc.p2p.R;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private String responseData = "";
    private String name = "";
    private static String password = "";
    private EditText email;
    private EditText passwordFirst; // 第一遍密码
    private EditText passwordFirstagain; // 重复密码
    private Boolean canClickEmail = true;  //Email是否合法
    private Boolean canClickPassWordSameSame = false;  //密码是否一致
    private Boolean canClickPassWordValid = false;  //密码是否是八到二十位字母和数字的组合

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 初始化注册的各组件，需要输入您的email（必须符合格式要求，不然不予注册）
     * 您想设定的密码并且再输入一次，确保两次输入的密码一致才可以注册
     * 有两个按钮，一个可以返回登录界面，另一个可以提交注册信息
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        email = (EditText) findViewById(R.id.UsernameReg);
        passwordFirst = (EditText) findViewById(R.id.PasswordReg);
        passwordFirstagain = (EditText) findViewById(R.id.PasswordRegAgain);
        Button buttonBack = (Button) findViewById(R.id.BackToLogin);
        Button buttonSubmit = (Button) findViewById(R.id.submit);
        TextView forgetPwd = (TextView) findViewById(R.id.forgetpwd);
        buttonBack.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RegisterActivity.this.onBackPressed();
                        // Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        // startActivity(intent);
                    }
                }
        );
        buttonSubmit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new check().isRight();
                        if (canClickEmail && canClickPassWordValid && canClickPassWordSameSame) {
                            ApolloClient apolloClient = ApolloClient.builder().serverUrl("http://49.232.12.147:4000").build();
                            final RegisterMutation register = RegisterMutation.builder()
                                    .username(email.getText().toString())
                                    .password(passwordFirst.getText().toString())
                                    .build();

                            apolloClient.mutate(register)
                                    .enqueue(new ApolloCall.Callback<RegisterMutation.Data>() {
                                        @Override
                                        public void onResponse(@NotNull Response<RegisterMutation.Data> response) {
                                            Log.d(TAG, response.getData().toString());
                                            if (response.getData().Register() != null) {
                                                Looper.prepare();
                                                showToast("注册成功，请点击已有账号登录！");
                                                Looper.loop();
                                            } else {
                                                Looper.prepare();
                                                showToast("注册失败，可能是账号已经注册过！");
                                                Looper.loop();
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NotNull ApolloException e) {
                                            Log.d(TAG, e.getLocalizedMessage(), e);
                                            Looper.prepare();
                                            showToast("注册失败！");
                                            Looper.loop();
                                        }
                                    });
                        } else if (!canClickEmail) {
                            showToast("邮箱不合法！");
                        } else if (!canClickPassWordValid) {
                            showToast("密码不合法，密码是八到二十位字母和数字的组合！");
                        } else {
                            showToast("两次输入的密码不一致！");
                        }
                    }
                }
        );


    }


    class check {

        /**
         * 判断是否符合要求
         */
        public void isRight() {
            String email = RegisterActivity.this.email.getText().toString();
            String password1 = RegisterActivity.this.passwordFirst.getText().toString();
            String password2 = RegisterActivity.this.passwordFirstagain.getText().toString();
            if (!same(password1, password2) || password1.isEmpty() || password2.isEmpty()) {
                RegisterActivity.this.canClickEmail = true;
                RegisterActivity.this.canClickPassWordSameSame = false;
            } else {
                String check = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z_]{8,20}$";
                Pattern regex = Pattern.compile(check);
                Matcher matcher = regex.matcher(password1);
                RegisterActivity.this.canClickEmail = true;
                RegisterActivity.this.canClickPassWordSameSame = true;
                if (matcher.matches())
                    RegisterActivity.this.canClickPassWordValid = true;
                else
                    RegisterActivity.this.canClickPassWordValid = false;
            }

        }

        /**
         * 使用正则表达式判断是否符合Email的标准
         *
         * @param string 传入要判断的字符串
         * @return 如果符合正则表达式，返回真，否则返回假
         */

        public boolean isEmail(String string) {
            if (string == null)
                return false;
            String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern p;
            Matcher m;
            p = Pattern.compile(regEx1);
            m = p.matcher(string);
            if (m.matches())
                return true;
            else
                return false;
        }

        /**
         * 判断两个密码是否相等
         *
         * @param pwd1 密码一
         * @param pwd2 密码二
         * @return 如果两个密码相等，返回真，否则返回假
         */

        public boolean same(String pwd1, String pwd2) {
            return pwd1.equals(pwd2);
        }

    }

}




