package edu.cit.mahumot.daycarelog.features.immunizations;

public class EpiVaccine {
    private final String name;
    private final int expectedDoses;

    public EpiVaccine(String name, int expectedDoses) {
        this.name = name;
        this.expectedDoses = expectedDoses;
    }

    public String getName() { return name; }
    public int getExpectedDoses() { return expectedDoses; }
}
