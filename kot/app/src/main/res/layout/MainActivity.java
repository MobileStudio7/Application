package com.tistory.webnautes.pickimageformgallery;

    import android.content.Intent;
    import android.net.Uri;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.ImageView;


    public class MainActivity extends AppCompatActivity {

    private final int GET_GALLERY_IMAGE = 200;
    private ImageView imageview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    imageview = (ImageView)findViewById(R.id.imageView);
    imageview.setOnClickListener(new View.OnClickListener() {
    public void onClick(View v) {

    Intent intent = new Intent(Intent.ACTION_PICK);
    intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
    startActivityForResult(intent, GET_GALLERY_IMAGE);
    }
    });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

    Uri selectedImageUri = data.getData();
    imageview.setImageURI(selectedImageUri);

    }

    }
    }