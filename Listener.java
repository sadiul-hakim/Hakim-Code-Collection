package org.javase;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;

public class Listener implements NativeKeyListener, NativeMouseListener {
    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        NativeKeyListener.super.nativeKeyPressed(nativeEvent);
        System.out.println(NativeKeyEvent.getKeyText(nativeEvent.getKeyCode()));
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeEvent) {
        NativeMouseListener.super.nativeMouseClicked(nativeEvent);
        System.out.println(nativeEvent.getButton());
    }

    public static void main(String[] args) throws NativeHookException {
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(new Listener()); // Add key listener
        GlobalScreen.addNativeMouseListener(new Listener()); // Add mouse listener
    }
}
