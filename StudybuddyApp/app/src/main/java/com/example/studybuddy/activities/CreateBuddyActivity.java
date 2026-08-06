package com.example.studybuddy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studybuddy.MainActivity;
import com.example.studybuddy.R;
import com.example.studybuddy.data.CourseData;
import com.example.studybuddy.models.Course;
import com.example.studybuddy.utils.MyConstants;
import com.example.studybuddy.utils.MyGlobals;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for creating a new AI study buddy specialized in a specific course.
 */
public class CreateBuddyActivity extends AppCompatActivity
{
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_BUDDIES = "buddies";

    private static final String FIELD_NAME = "name";
    private static final String FIELD_COURSE_ID = "courseId";
    private static final String FIELD_COURSE_NAME = "courseName";
    private static final String FIELD_COURSE_EMOJI = "courseEmoji";
    private static final String FIELD_COURSE_COLOR = "courseColor";
    private static final String FIELD_QUADRIMESTER = "quadrimester";
    private static final String FIELD_SYSTEM_PROMPT = "systemPrompt";
    private static final String FIELD_CREATED_AT = "createdAt";

    private TextInputEditText etName;
    private Spinner spinnerCourse;
    private TextView tvPreviewName, tvPreviewCourse, tvPreviewPrompt, tvAvatar;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String currentUid;

    private Course selectedCourse;
    private List<Course> courseList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_buddy);

        MaterialToolbar toolbar = findViewById(R.id.buddy_toolbar);
        MyGlobals.setupToolbar(this, toolbar);

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        etName = findViewById(R.id.et_buddy_name);
        spinnerCourse = findViewById(R.id.spinner_course);
        tvPreviewName = findViewById(R.id.tv_preview_name);
        tvPreviewCourse = findViewById(R.id.tv_preview_course);
        tvPreviewPrompt = findViewById(R.id.tv_preview_prompt);
        tvAvatar = findViewById(R.id.tv_avatar);
        progressBar = findViewById(R.id.progress_create);

        setupCourseSpinner();

        Button btnCreate = findViewById(R.id.btn_create_buddy);
        btnCreate.setOnClickListener(v -> createBuddy());

        etName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                updatePreview();
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });
    }

    /**
     * Initializes the course selection spinner.
     */
    private void setupCourseSpinner()
    {
        courseList = CourseData.getBAC2Courses();

        List<String> labels = new ArrayList<>();
        for (Course c : courseList)
        {
            labels.add(c.getName() + "  (" + c.getQuadrimester() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(adapter);

        if (!courseList.isEmpty())
        {
            selectedCourse = courseList.get(MyConstants.ZERO);
        }
        updatePreview();

        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                selectedCourse = courseList.get(position);
                tvAvatar.setText("");
                updatePreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });
    }

    /**
     * Updates the buddy preview UI based on user input.
     */
    private void updatePreview()
    {
        String nameInput = (etName.getText() != null) ? etName.getText().toString().trim() : "";
        String displayName = nameInput.isEmpty() ? "My Buddy" : nameInput;

        tvPreviewName.setText(displayName);

        if (selectedCourse != null)
        {
            String courseText = "Specialty: " + selectedCourse.getName() + " (" + selectedCourse.getQuadrimester() + ")";
            tvPreviewCourse.setText(courseText);

            String promptText = "\"" + buildSystemPrompt(displayName, selectedCourse) + "\"";
            tvPreviewPrompt.setText(promptText);
        }
    }

    /**
     * Constructs the system prompt for the AI based on its specialty.
     */
    private String buildSystemPrompt(String buddyName, Course course)
    {
        return "You are " + buddyName + ", a pedagogical assistant specialized in "
                + course.getName() + " for Bac 2 Computer Science students at HELB Brussels. "
                + "You explain concepts clearly, with concrete examples and code if necessary. "
                + "You ask questions to check understanding. "
                + "You only respond in relation to the " + course.getName() + " course. "
                + "You are encouraging and adapt your level to the student.";
    }

    /**
     * Validates input and saves the new buddy to Firestore.
     */
    private void createBuddy()
    {
        String name = (etName.getText() != null) ? etName.getText().toString().trim() : "";

        if (name.isEmpty())
        {
            etName.setError("Please give your buddy a name!");
            return;
        }

        if (selectedCourse == null)
        {
            Toast.makeText(this, "Please select a course!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String systemPrompt = buildSystemPrompt(name, selectedCourse);

        Map<String, Object> buddyData = new HashMap<>();
        buddyData.put(FIELD_NAME, name);
        buddyData.put(FIELD_COURSE_ID, selectedCourse.getId());
        buddyData.put(FIELD_COURSE_NAME, selectedCourse.getName());
        buddyData.put(FIELD_COURSE_EMOJI, selectedCourse.getEmoji());
        buddyData.put(FIELD_COURSE_COLOR, selectedCourse.getColor());
        buddyData.put(FIELD_QUADRIMESTER, selectedCourse.getQuadrimester());
        buddyData.put(FIELD_SYSTEM_PROMPT, systemPrompt);
        buddyData.put(FIELD_CREATED_AT, FieldValue.serverTimestamp());

        db.collection(COLLECTION_USERS)
                .document(currentUid)
                .collection(COLLECTION_BUDDIES)
                .add(buddyData)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, name + " created! Happy studying!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
