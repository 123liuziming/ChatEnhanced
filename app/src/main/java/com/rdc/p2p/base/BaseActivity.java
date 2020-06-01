package com.rdc.p2p.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.rdc.p2p.bean.MyDnsBean;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import butterknife.ButterKnife;

public abstract class BaseActivity<T extends BasePresenter> extends AppCompatActivity implements BaseContract.View {

    protected T presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = getInstance();
        if (presenter!=null){
            presenter.attachView(this);
        }
        setContentView(setLayoutResID());
        ButterKnife.bind(this);
        initData();
        initView();
        initListener();
        LitePal.getDatabase();
    }

    @Override
    protected void onDestroy() {
        if (presenter != null) {
            presenter.detachView();
        }
        //DataSupport.deleteAll(MyDnsBean.class);
        super.onDestroy();
    }

    public abstract T getInstance();

    protected abstract int setLayoutResID();

    protected abstract void initData();

    protected abstract void initView();

    protected abstract void initListener();

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    protected static String getString(EditText et) {
        return et.getText().toString();
    }



}
