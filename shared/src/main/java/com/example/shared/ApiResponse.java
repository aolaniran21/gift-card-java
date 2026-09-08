package com.example.shared;

public record ApiResponse<T>(String message, T data) {
}
