package com.example.studybuddy.data;

import com.example.studybuddy.models.Course;

import java.util.Arrays;
import java.util.List;

public class CourseData
{
    public static List<Course> getBAC2Courses()
    {
        return Arrays.asList(

                new Course(
                        "java3",
                        "Java Programming III",
                        "UE3-1",
                        "☕",
                        "Q3",
                        "#E65100"   // orange Java
                ),
                new Course(
                        "dotnet2",
                        ".NET / C# Development II",
                        "UE3-1",
                        "🔷",
                        "Q3",
                        "#1565C0"   // Microsoft blue
                ),
                new Course(
                        "algo2",
                        "Algorithms II",
                        "UE3-1",
                        "🧮",
                        "Q3",
                        "#6A1B9A"   // purple
                ),
                new Course(
                        "web2",
                        "Web Programming II",
                        "UE3-4",
                        "🌐",
                        "Q3",
                        "#00838F"   // cyan web
                ),
                new Course(
                        "archi3",
                        "Computer Architecture III",
                        "UE3-3",
                        "📟",
                        "Q3",
                        "#37474F"   // hardware grey
                ),
                new Course(
                        "analyse2",
                        "Analysis II",
                        "UE3-1",
                        "📈",
                        "Q3",
                        "#2E7D32"   // analysis green
                ),
                new Course(
                        "math2",
                        "Applied Mathematics II",
                        "UE2-5",
                        "📐",
                        "Q3",
                        "#AD1457"   // maths pink
                ),
                new Course(
                        "sysex2",
                        "Operating Systems II",
                        "UE3-1",
                        "🐧",
                        "Q3",
                        "#4E342E"   // OS brown
                ),
                new Course(
                        "proba1",
                        "Probability and Statistics I",
                        "UE3-1",
                        "🎲",
                        "Q3",
                        "#0277BD"   // stats blue
                ),
                new Course(
                        "english3q",
                        "English II (Professional)",
                        "UE2-6",
                        "🇬🇧",
                        "Q3",
                        "#558B2F"   // English green
                ),


                new Course(
                        "java4",
                        "Java Programming IV",
                        "UE4-1",
                        "☕",
                        "Q4",
                        "#BF360C"   // dark orange Java
                ),
                new Course(
                        "dotnet3",
                        ".NET / C# Development III",
                        "UE4-1",
                        "🔷",
                        "Q4",
                        "#0D47A1"   // dark blue .NET
                ),
                new Course(
                        "mobile1",
                        "Mobile Development I",
                        "UE4-2",
                        "📱",
                        "Q4",
                        "#1B5E20"   // Android green
                ),
                new Course(
                        "webmobile1",
                        "Web and Mobile Programming I",
                        "UE4-2",
                        "🕸️",
                        "Q4",
                        "#006064"   // dark cyan
                ),
                new Course(
                        "si2",
                        "Information Systems II",
                        "UE4-3",
                        "📊",
                        "Q4",
                        "#4A148C"   // dark purple
                ),
                new Course(
                        "bdd2",
                        "Database Systems II",
                        "UE4-1",
                        "🗄️",
                        "Q4",
                        "#E65100"   // DB orange
                ),
                new Course(
                        "gestion_proj1",
                        "Project Management I",
                        "UE4-4",
                        "📅",
                        "Q4",
                        "#33691E"   // project green
                ),
                new Course(
                        "math3",
                        "Applied Mathematics III",
                        "UE4-5",
                        "♾️",
                        "Q4",
                        "#880E4F"   // dark pink maths
                ),
                new Course(
                        "marketing",
                        "Marketing and e-Commerce",
                        "UE4-6",
                        "📢",
                        "Q4",
                        "#F57F17"   // marketing yellow
                ),
                new Course(
                        "english4",
                        "Business English III",
                        "UE3-7",
                        "🇬🇧",
                        "Q4",
                        "#1B5E20"   // English green
                )
        );
    }

    // Retrieve only the courses for a given quadrimester
    public static List<Course> getCoursesByQuad(String quad)
    {
        List<Course> all = getBAC2Courses();
        List<Course> filtered = new java.util.ArrayList<>();
        for (Course c : all)
        {
            if (c.getQuadrimester().equals(quad))
            {
                filtered.add(c);
            }
        }
        return filtered;
    }

    public static Course getCourseById(String id)
    {
        for (Course c : getBAC2Courses())
        {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }
}