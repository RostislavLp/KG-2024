import tkinter as tk
from tkinter import filedialog

import cv2
import numpy as np
from PIL import Image, ImageTk

original_image = None
current_image = None
image_on_canvas = None


def select_image():
    global original_image, current_image, image_on_canvas, canvas

    img_path = filedialog.askopenfilename()

    if len(img_path) > 0:
        image = cv2.imread(img_path)

        if image is not None:
            original_image = image.copy()
            current_image = image.copy()

            image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            image_pil = Image.fromarray(image_rgb)
            image_tk = ImageTk.PhotoImage(image_pil)

            if image_on_canvas is not None:
                canvas.delete(image_on_canvas)

            image_on_canvas = canvas.create_image(0, 0, anchor='nw', image=image_tk)
            canvas.image = image_tk

            canvas.config(scrollregion=canvas.bbox(tk.ALL))

        else:
            print("Ошибка загрузки изображения. Пожалуйста, выберите другое изображение.")


def linear_contrast():
    global current_image, image_on_canvas, canvas
    if current_image is not None:
        b_channel, g_channel, r_channel = cv2.split(current_image)

        b_channel = cv2.add(b_channel, 50)  
        g_channel = cv2.add(g_channel, 50)  
        r_channel = cv2.add(r_channel, 50) 

        b_channel = cv2.multiply(b_channel, 1.2)  
        g_channel = cv2.multiply(g_channel, 1.2) 
        r_channel = cv2.multiply(r_channel, 1.2) 

        enhanced_image = cv2.merge((b_channel, g_channel, r_channel))

        f_image = enhanced_image.astype(np.float32)

        min_val = np.min(f_image)
        max_val = np.max(f_image)

        scaled_image = 255 * (f_image - min_val) / (max_val - min_val)
        scaled_image = np.clip(scaled_image, 0, 255).astype(np.uint8)

        result_image_rgb = cv2.cvtColor(scaled_image, cv2.COLOR_BGR2RGB) 
        result_image_rgb = Image.fromarray(result_image_rgb)
        result_image_tk = ImageTk.PhotoImage(result_image_rgb)

        if image_on_canvas is not None:
            canvas.delete(image_on_canvas)
        image_on_canvas = canvas.create_image(0, 0, anchor='nw', image=result_image_tk)
        canvas.image = result_image_tk
    else:
        print("Нет изображения для обработки.")


def equalize_rgb():
    global current_image, image_on_canvas, canvas
    if current_image is not None:
        r_channel, g_channel, b_channel = cv2.split(current_image)

        r_eq = cv2.equalizeHist(r_channel)
        g_eq = cv2.equalizeHist(g_channel)
        b_eq = cv2.equalizeHist(b_channel)

        equalized_rgb_image = cv2.merge((r_eq, g_eq, b_eq))

        display_image(equalized_rgb_image)
    else:
        print("Нет изображения для обработки.")


def equalize_hsv():
    global current_image, image_on_canvas, canvas
    if current_image is not None:
        hsv_image = cv2.cvtColor(current_image, cv2.COLOR_BGR2HSV)

        h, s, v = cv2.split(hsv_image)
        v_eq = cv2.equalizeHist(v)
        equalized_hsv_image = cv2.merge((h, s, v_eq))

        equalized_bgr_image = cv2.cvtColor(equalized_hsv_image, cv2.COLOR_HSV2BGR)

        display_image(equalized_bgr_image)
    else:
        print("Нет изображения для обработки.")


def reset_image():
    global original_image, current_image, image_on_canvas, canvas
    if original_image is not None:
        current_image = original_image.copy()  
        display_image(current_image)


def display_image(image):
    global image_on_canvas
    image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)  
    image_pil = Image.fromarray(image_rgb)
    image_tk = ImageTk.PhotoImage(image_pil)

    if image_on_canvas is not None:
        canvas.delete(image_on_canvas)

    image_on_canvas = canvas.create_image(0, 0, anchor='nw', image=image_tk)
    canvas.image = image_tk


root = tk.Tk()
root.title("Image processing using OpenCV")

title = tk.Label(root, text="Image processing using OpenCV", font=("Helvetica", 16))
title.pack(padx=10, pady=10)

canvas = tk.Canvas(root, width=500, height=500)
canvas.pack(side=tk.LEFT, expand=True, fill=tk.BOTH)

v_scrollbar = tk.Scrollbar(root, orient=tk.VERTICAL, command=canvas.yview)
v_scrollbar.pack(side=tk.RIGHT, fill=tk.Y)

h_scrollbar = tk.Scrollbar(root, orient=tk.HORIZONTAL, command=canvas.xview)
h_scrollbar.pack(side=tk.BOTTOM, fill=tk.X)

canvas.config(yscrollcommand=v_scrollbar.set, xscrollcommand=h_scrollbar.set)

button_frame = tk.Frame(root)
button_frame.pack(side=tk.RIGHT, padx=10, pady=10)

btn_select = tk.Button(button_frame, text="Выбрать изображение", command=select_image)
btn_select.pack(fill=tk.X, pady=5)

btn_contrast = tk.Button(button_frame, text="Поэлементные операции\nи линейное контрастирование", command=linear_contrast)
btn_contrast.pack(fill=tk.X, pady=5)

btn_hist_rgb = tk.Button(button_frame, text="Эквализация гистограммы RGB", command=equalize_rgb)
btn_hist_rgb.pack(fill=tk.X, pady=5)

btn_hist_hsv = tk.Button(button_frame, text="Эквализация гистограммы в HSV", command=equalize_hsv)
btn_hist_hsv.pack(fill=tk.X, pady=5)

btn_reset = tk.Button(button_frame, text="Сбросить изображение", command=reset_image)
btn_reset.pack(fill=tk.X, pady=5)

root.mainloop()