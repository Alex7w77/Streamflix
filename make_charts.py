#!/usr/bin/env python3
"""Genera los graficos del informe StreamFlix con estilo moderno y coherente."""
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib import font_manager
import numpy as np
import os

OUT = "charts"
os.makedirs(OUT, exist_ok=True)

# ---- Paleta coherente con la portada del informe ----
NAVY   = "#1A5276"
BLUE   = "#2E86C1"
TEAL   = "#17A589"
AMBER  = "#E59866"
CORAL  = "#E74C3C"
GREY   = "#AEB6BF"
LIGHT  = "#EBF5FB"
TEXT   = "#34495E"

plt.rcParams.update({
    "font.family": "DejaVu Sans",
    "font.size": 12,
    "text.color": TEXT,
    "axes.labelcolor": TEXT,
    "xtick.color": TEXT,
    "ytick.color": TEXT,
    "axes.edgecolor": GREY,
    "axes.linewidth": 0.8,
    "figure.dpi": 150,
})

def style(ax, grid_axis="y"):
    ax.spines["top"].set_visible(False)
    ax.spines["right"].set_visible(False)
    ax.grid(axis=grid_axis, color=GREY, alpha=0.35, linewidth=0.7)
    ax.set_axisbelow(True)

def save(fig, name):
    fig.tight_layout()
    fig.savefig(os.path.join(OUT, name), bbox_inches="tight", facecolor="white")
    plt.close(fig)
    print("saved", name)

# 1) MAE: baseline vs propuesto -------------------------------------------------
fig, ax = plt.subplots(figsize=(7.2, 3.0))
vals = [1.6736, 0.9132]
labels = ["Sistema Actual\n(Baseline)", "Sistema Propuesto\n(Hibrido + IA)"]
colors = [GREY, NAVY]
bars = ax.barh(labels, vals, color=colors, height=0.55)
for b, v in zip(bars, vals):
    ax.text(v + 0.03, b.get_y() + b.get_height()/2, f"{v:.4f}",
            va="center", fontweight="bold", color=TEXT)
ax.set_xlim(0, 2.0)
ax.set_xlabel("MAE (Error Absoluto Medio) - menor es mejor")
ax.set_title("Reduccion del error de prediccion: -45.4%", fontweight="bold", color=NAVY, pad=12)
style(ax, "x")
save(fig, "mae_comparison.png")

# 2) Metricas por fold ----------------------------------------------------------
folds = ["F1", "F2", "F3", "F4", "F5"]
mae = [0.7913, 0.7546, 1.0240, 0.9772, 1.0191]
f1  = [0.1489, 0.1386, 0.1066, 0.1742, 0.1662]
fig, ax = plt.subplots(figsize=(7.2, 3.4))
x = np.arange(len(folds)); w = 0.38
b1 = ax.bar(x - w/2, mae, w, label="MAE", color=NAVY)
b2 = ax.bar(x + w/2, f1, w, label="F1-Score", color=TEAL)
ax.axhline(0.9132, ls="--", lw=1.2, color=CORAL, alpha=0.8)
ax.text(4.3, 0.93, "MAE prom. 0.913", color=CORAL, fontsize=9, ha="right")
ax.set_xticks(x); ax.set_xticklabels(folds)
ax.set_ylabel("Valor de la metrica")
ax.set_title("Estabilidad del modelo en 5-Fold Cross-Validation", fontweight="bold", color=NAVY, pad=12)
ax.legend(frameon=False, ncol=2, loc="upper center", bbox_to_anchor=(0.5, -0.12))
style(ax)
save(fig, "folds_metrics.png")

# 3) Preferencias por grupo etario (rating del genero top) ----------------------
groups = ["18-25", "26-35", "36-45", "46+"]
top_genre = ["Animacion", "Ciencia Ficcion", "Suspenso", "Romance"]
top_rating = [4.36, 3.55, 3.75, 3.75]
fig, ax = plt.subplots(figsize=(7.2, 3.4))
bars = ax.bar(groups, top_rating, color=[BLUE, TEAL, AMBER, CORAL], width=0.6)
for b, g, r in zip(bars, top_genre, top_rating):
    ax.text(b.get_x()+b.get_width()/2, r+0.05, g, ha="center", fontsize=9, fontweight="bold", color=TEXT)
    ax.text(b.get_x()+b.get_width()/2, r/2, f"{r:.2f}", ha="center", color="white", fontweight="bold")
ax.set_ylim(0, 5)
ax.set_ylabel("Rating promedio del genero preferido")
ax.set_xlabel("Grupo etario")
ax.set_title("Genero favorito por grupo de edad", fontweight="bold", color=NAVY, pad=12)
style(ax)
save(fig, "age_genre.png")

# 4) Patrones por suscripcion ---------------------------------------------------
subs = ["Basic", "Standard", "Premium"]
movies = [14.9, 21.7, 30.0]
hours = [15.6, 9.8, 7.0]
fig, ax = plt.subplots(figsize=(7.2, 3.4))
x = np.arange(len(subs)); w = 0.38
ax.bar(x - w/2, movies, w, label="Peliculas vistas (prom.)", color=NAVY)
ax.bar(x + w/2, hours, w, label="Horas semanales (prom.)", color=AMBER)
ax.set_xticks(x); ax.set_xticklabels(subs)
ax.set_title("Comportamiento por tipo de suscripcion", fontweight="bold", color=NAVY, pad=12)
ax.legend(frameon=False, ncol=2, loc="upper center", bbox_to_anchor=(0.5, -0.12))
style(ax)
save(fig, "subscription.png")

# 5) Tasa de completamiento por genero -----------------------------------------
g = ["Drama","Ciencia Ficcion","Romance","Documental","Suspenso","Comedia",
     "Animacion","Crimen","Fantasia","Terror","Accion","Aventura"]
rate = [55.5,55.1,54.5,53.1,48.1,46.8,45.0,40.0,34.4,33.3,32.5,20.0]
order = np.argsort(rate)
g = [g[i] for i in order]; rate = [rate[i] for i in order]
cmap = [TEAL if r>=50 else (AMBER if r>=40 else CORAL) for r in rate]
fig, ax = plt.subplots(figsize=(7.2, 4.2))
bars = ax.barh(g, rate, color=cmap, height=0.66)
for b, r in zip(bars, rate):
    ax.text(r+0.6, b.get_y()+b.get_height()/2, f"{r:.1f}%", va="center", fontsize=9, color=TEXT)
ax.set_xlim(0, 65)
ax.set_xlabel("Tasa de completamiento (%)")
ax.set_title("Que generos se terminan de ver?", fontweight="bold", color=NAVY, pad=12)
style(ax, "x")
save(fig, "completion.png")

# 6) Satisfaccion baseline vs propuesto ----------------------------------------
cats = ["Relevancia","Variedad","Velocidad","Facilidad\nde uso","Descubri-\nmiento","Precision"]
base = [3.6,3.0,3.5,3.1,3.2,3.9]
prop = [4.5,4.1,4.7,4.1,4.6,4.2]
fig, ax = plt.subplots(figsize=(7.6, 3.6))
x = np.arange(len(cats)); w = 0.4
ax.bar(x - w/2, base, w, label="Baseline", color=GREY)
ax.bar(x + w/2, prop, w, label="Propuesto", color=NAVY)
ax.set_xticks(x); ax.set_xticklabels(cats, fontsize=9)
ax.set_ylim(0, 5)
ax.set_ylabel("Puntuacion (1-5)")
ax.set_title("Satisfaccion del usuario por dimension", fontweight="bold", color=NAVY, pad=12)
ax.legend(frameon=False, ncol=2, loc="upper center", bbox_to_anchor=(0.5, -0.18))
style(ax)
save(fig, "satisfaction.png")

# 7) Industria: % de consumo desde recomendaciones -----------------------------
plat = ["Netflix","YouTube","Disney+","Spotify","Amazon\nPrime"]
pct = [80, 70, 45, 40, 35]
fig, ax = plt.subplots(figsize=(7.2, 3.2))
bars = ax.bar(plat, pct, color=[NAVY, BLUE, TEAL, AMBER, CORAL], width=0.62)
for b, p in zip(bars, pct):
    ax.text(b.get_x()+b.get_width()/2, p+1.5, f"{p}%", ha="center", fontweight="bold", color=TEXT)
ax.set_ylim(0, 95)
ax.set_ylabel("% del consumo via recomendaciones")
ax.set_title("Impacto de las recomendaciones en la industria", fontweight="bold", color=NAVY, pad=12)
style(ax)
save(fig, "industry.png")

print("ALL CHARTS DONE")
