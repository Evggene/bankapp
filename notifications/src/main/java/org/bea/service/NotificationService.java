package org.bea.service;

import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class NotificationService {

    public static final Queue<String> messages = new ConcurrentLinkedQueue<>();
}
