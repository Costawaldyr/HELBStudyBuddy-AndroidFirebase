package com.example.studybuddy.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.studybuddy.R;
import com.example.studybuddy.data.CourseData;
import com.example.studybuddy.models.Buddy;
import com.example.studybuddy.models.Course;
import com.example.studybuddy.utils.MyConstants;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateBuddyBottomSheet extends BottomSheetDialogFragment
{
    private static final String ARG_BUDDY_ID = "buddy_id";
    private static final String ARG_BUDDY_NAME = "buddy_name";
    private static final String ARG_COURSE_ID = "course_id";

    private TextInputEditText etBuddyName;
    private Spinner spinnerCourse;
    private ImageView ivSelectedIcon, ivPreviewIcon, imgBuddyPhoto;
    private TextView tvPreviewName, tvPreviewCourse;
    private MaterialButton btnCreate;
    private ProgressBar progressBar;
    private MaterialCardView cardEmojiPicker;

    private Course selectedCourse;
    private String buddyIdToEdit = null;
    private Uri selectedImageUri = null;
    private String existingImageUrl = null;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String currentUid;

    public static CreateBuddyBottomSheet newInstance()
    {
        return new CreateBuddyBottomSheet();
    }

    public static CreateBuddyBottomSheet newInstanceForEdit(String id, String name, String courseId)
    {
        CreateBuddyBottomSheet fragment = new CreateBuddyBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_BUDDY_ID, id);
        args.putString(ARG_BUDDY_NAME, name);
        args.putString(ARG_COURSE_ID, courseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.dialog_create_buddy, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initViews(view);
        setupCourseSpinner();
        setupPhotoPicker(view);

        if (getArguments() != null)
        {
            buddyIdToEdit = getArguments().getString(ARG_BUDDY_ID);
            etBuddyName.setText(getArguments().getString(ARG_BUDDY_NAME));
            btnCreate.setText(getString(R.string.update));
        }

        btnCreate.setOnClickListener(v -> saveBuddy());

        etBuddyName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                tvPreviewName.setText(s.length() > MyConstants.ZERO ? s.toString() : getString(R.string.my_buddy_default));
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });
    }

    private void initViews(View view)
    {
        etBuddyName = view.findViewById(R.id.et_buddy_name);
        spinnerCourse = view.findViewById(R.id.spinner_course);
        ivSelectedIcon = view.findViewById(R.id.iv_selected_icon);
        ivPreviewIcon = view.findViewById(R.id.iv_preview_icon);
        tvPreviewName = view.findViewById(R.id.tv_preview_name);
        tvPreviewCourse = view.findViewById(R.id.tv_preview_course);
        imgBuddyPhoto = view.findViewById(R.id.img_buddy_photo);
        btnCreate = view.findViewById(R.id.btn_create_buddy);
        progressBar = view.findViewById(R.id.progress_create);
        cardEmojiPicker = view.findViewById(R.id.card_emoji_picker);
    }

    private void setupCourseSpinner()
    {
        List<Course> courses = CourseData.getBAC2Courses();
        List<String> courseNames = new ArrayList<>();
        for (Course c : courses)
        {
            courseNames.add(c.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, courseNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(adapter);

        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                selectedCourse = courses.get(position);
                tvPreviewCourse.setText(selectedCourse.getName());
                updateAvatarPreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        if (getArguments() != null)
        {
            String courseId = getArguments().getString(ARG_COURSE_ID);
            for (int i = MyConstants.ZERO; i < courses.size(); i++)
            {
                if (courses.get(i).getId().equals(courseId))
                {
                    spinnerCourse.setSelection(i);
                    break;
                }
            }
        }
    }

    private void updateAvatarPreview()
    {
        if (selectedImageUri != null)
        {
            imgBuddyPhoto.setImageURI(selectedImageUri);
            imgBuddyPhoto.setVisibility(View.VISIBLE);
            ivSelectedIcon.setVisibility(View.GONE);
            ivPreviewIcon.setImageURI(selectedImageUri);
            ivPreviewIcon.setPadding(MyConstants.ZERO, MyConstants.ZERO, MyConstants.ZERO, MyConstants.ZERO);
        }
        else if (selectedCourse != null && selectedCourse.getEmoji() != null)
        {
            imgBuddyPhoto.setVisibility(View.GONE);
            ivSelectedIcon.setVisibility(View.VISIBLE);

            Bitmap emojiBitmap = renderEmojiOnCircle(selectedCourse.getEmoji(), selectedCourse.getColor());
            ivSelectedIcon.setImageBitmap(emojiBitmap);
            ivPreviewIcon.setImageBitmap(emojiBitmap);

            ivSelectedIcon.setPadding(MyConstants.ZERO, MyConstants.ZERO, MyConstants.ZERO, MyConstants.ZERO);
            ivPreviewIcon.setPadding(MyConstants.ZERO, MyConstants.ZERO, MyConstants.ZERO, MyConstants.ZERO);
            ivSelectedIcon.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private Bitmap renderEmojiOnCircle(String emoji, String colorHex)
    {
        float density = getResources().getDisplayMetrics().density;
        int sizePx = (int) (MyConstants.DEFAULT_AVATAR_DP * density);
        Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        try
        {
            bg.setColor(Color.parseColor(colorHex));
        }
        catch (Exception e)
        {
            bg.setColor(MyConstants.DEFAULT_BG_COLOR);
        }
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, bg);

        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setTextSize(sizePx * MyConstants.EMOJI_TEXT_SIZE_RATIO);
        text.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = text.getFontMetrics();
        float baseline = sizePx / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(emoji, sizePx / 2f, baseline, text);
        return bmp;
    }

    private void setupPhotoPicker(View view)
    {
        ActivityResultLauncher<Intent> pickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result ->
                {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null)
                    {
                        selectedImageUri = result.getData().getData();
                        updateAvatarPreview();
                    }
                }
        );

        View btnPick = view.findViewById(R.id.btn_pick_photo);
        if (btnPick != null)
        {
            btnPick.setOnClickListener(v ->
            {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                pickerLauncher.launch(intent);
            });
        }
    }

    private void saveBuddy()
    {
        String name = etBuddyName.getText().toString().trim();
        if (name.isEmpty())
        {
            etBuddyName.setError(getString(R.string.give_buddy_name_error));
            return;
        }

        if (selectedCourse == null)
        {
            Toast.makeText(getContext(), getString(R.string.select_course_error), Toast.LENGTH_SHORT).show();
            return;
        }

        btnCreate.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (selectedImageUri != null)
        {
            uploadImageAndSaveBuddy(name);
        }
        else
        {
            finalizeSaveBuddy(name, existingImageUrl);
        }
    }

    private void uploadImageAndSaveBuddy(String name)
    {
        String fileName = "buddies/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference().child(fileName);

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri ->
                {
                    finalizeSaveBuddy(name, uri.toString());
                }))
                .addOnFailureListener(e ->
                {
                    Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                    finalizeSaveBuddy(name, null);
                });
    }

    private void finalizeSaveBuddy(String name, String imageUrl)
    {
        Buddy buddy = new Buddy();
        buddy.setName(name);
        buddy.setCourseId(selectedCourse.getId());
        buddy.setCourseName(selectedCourse.getName());
        buddy.setCourseEmoji(selectedCourse.getEmoji());
        buddy.setCourseColor(selectedCourse.getColor());
        buddy.setQuadrimester(selectedCourse.getQuadrimester());
        buddy.setSystemPrompt(getString(R.string.buddy_system_prompt_template, name, selectedCourse.getName()));
        buddy.setImageUrl(imageUrl);
        buddy.setIconResId(MyConstants.ZERO);

        if (buddyIdToEdit != null)
        {
            buddy.setId(buddyIdToEdit);
            db.collection(MyConstants.COLLECTION_USERS).document(currentUid).collection(MyConstants.COLLECTION_BUDDIES).document(buddyIdToEdit)
                    .set(buddy)
                    .addOnSuccessListener(aVoid -> dismiss())
                    .addOnFailureListener(e ->
                    {
                        btnCreate.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), getString(R.string.error_updating_buddy), Toast.LENGTH_SHORT).show();
                    });
        }
        else
        {
            db.collection(MyConstants.COLLECTION_USERS).document(currentUid).collection(MyConstants.COLLECTION_BUDDIES)
                    .add(buddy)
                    .addOnSuccessListener(documentReference ->
                    {
                        buddy.setId(documentReference.getId());
                        documentReference.set(buddy);
                        dismiss();
                    })
                    .addOnFailureListener(e ->
                    {
                        btnCreate.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), getString(R.string.error_creating_buddy), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
