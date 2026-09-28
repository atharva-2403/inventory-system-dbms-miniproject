package com.example.inventory.model;

import java.time.LocalDate;

public record Employee(int id, String firstName, String lastName, LocalDate dob) {}
