package com.example.studybuddy.data;


import com.example.studybuddy.models.StudyLocation;

import java.util.Arrays;
import java.util.List;

public class StudyLocationData
{
    public static List<StudyLocation> getStudyLocations()
    {
        return Arrays.asList(
                new StudyLocation(
                        "HELB La Plaine",
                        "Campus de la Plaine, Brussels",
                        50.8209,
                        4.3970
                ),
                new StudyLocation(
                        "HELB Library",
                        "HELB Library, Brussels",
                        50.8215,
                        4.3965
                ),
                new StudyLocation(
                        "ULB La Plaine",
                        "Campus ULB La Plaine, Brussels",
                        50.8139,
                        4.3813
                ),
                new StudyLocation(
                        "VUB",
                        "Vrije Universiteit Brussel, Etterbeek",
                        50.8218,
                        4.3947
                ),
                new StudyLocation(
                        "Coworking / Study Café",
                        "Avenue Franklin Roosevelt, Brussels",
                        50.8148,
                        4.3735
                ),
                new StudyLocation(
                        "Bibliothèque Solvay",
                        "Parc Léopold, Brussels",
                        50.8400,
                        4.3770
                ),
                new StudyLocation(
                        "Muntpunt Library",
                        "Place de la Monnaie, Brussels",
                        50.8495,
                        4.3540
                ),
                new StudyLocation(
                        "Study Spot Grand Place",
                        "Grand Place, Brussels",
                        50.8467,
                        4.3525
                ),
                new StudyLocation(
                        "Parc de Bruxelles Study",
                        "Parc de Bruxelles, Brussels",
                        50.8445,
                        4.3635
                ),
                new StudyLocation(
                        "Fake Study Hub North",
                        "Boulevard du Jardin Botanique, Brussels",
                        50.8550,
                        4.3600
                ),
                new StudyLocation(
                        "Fake Study Hub South",
                        "Avenue de la Couronne, Ixelles",
                        50.8250,
                        4.3850
                ),
                new StudyLocation(
                        "Fake Study Hub West",
                        "Place de la Reine, Schaerbeek",
                        50.8590,
                        4.3730
                )
        );
    }
}