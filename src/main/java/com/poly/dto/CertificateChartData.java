package com.poly.dto;

import java.util.List;

public class CertificateChartData {
    private List<String> labels;
    private List<Double> data;
    private List<String> backgroundColor;

    public CertificateChartData(List<String> labels, List<Double> data, List<String> backgroundColor) {
        this.labels = labels;
        this.data = data;
        this.backgroundColor = backgroundColor;
    }

    // Getters
    public List<String> getLabels() {
        return labels;
    }

    public List<Double> getData() {
        return data;
    }

    public List<String> getBackgroundColor() {
        return backgroundColor;
    }
}