package edu.cit.mahumot.daycarelog.features.immunizations;

import java.util.List;
import java.util.Map;

// DOH Expanded Program on Immunization (EPI) schedule for children 0-5.
final class EpiVaccineSchedule {

    static final List<EpiVaccine> ALL = List.of(
        new EpiVaccine("BCG", 1),
        new EpiVaccine("Hepatitis B", 1),
        new EpiVaccine("Pentavalent", 3),
        new EpiVaccine("OPV", 3),
        new EpiVaccine("IPV", 1),
        new EpiVaccine("PCV", 3),
        new EpiVaccine("MMR", 2)
    );

    private static final Map<String, Integer> EXPECTED_DOSES_BY_NAME = ALL.stream()
            .collect(java.util.stream.Collectors.toMap(EpiVaccine::getName, EpiVaccine::getExpectedDoses));

    private EpiVaccineSchedule() {}

    static boolean isKnownVaccine(String name) {
        return EXPECTED_DOSES_BY_NAME.containsKey(name);
    }

    static Integer expectedDoses(String name) {
        return EXPECTED_DOSES_BY_NAME.get(name);
    }
}
