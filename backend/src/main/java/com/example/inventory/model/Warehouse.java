package com.example.inventory.model;

public record Warehouse(int id, String street, String city, String state, String pincode, int capacity, Integer managerId) {}
