package com.networks;

import java.io.BufferedWriter;

public record UserInfo(BufferedWriter writer, UserStatus status) {}
