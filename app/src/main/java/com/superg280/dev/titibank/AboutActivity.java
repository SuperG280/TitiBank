package com.superg280.dev.titibank;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView twCopyright = findViewById( R.id.textView_about_copyrigth);
        TextView twVersion = findViewById(R.id.textView_about_version);
        TextView tvDescripcion = findViewById( R.id.textView_about_descripcion);
        twCopyright.setText( getString(R.string.copyrigth));
        twVersion.setText( getString(R.string.about_version));
        tvDescripcion.setText( getString(R.string.about_descripcion));

        final ImageView icon = findViewById(R.id.imageView_about);

        icon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Animation rotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim);
                icon.startAnimation(rotateAnimation);
            }
        });
    }
}
