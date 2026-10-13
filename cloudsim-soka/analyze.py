import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

df = pd.read_csv("summary.csv")
order = ["RR", "ABC", "CS", "RR-ABC", "GA-GWO"]
task_counts = sorted(df["tasks"].unique())
for metric, err, title in [("mk_mean", "mk_std", "Makespan (s)"), ("util_mean", "util_std", "Resource Utilization (%)"), ("wait_mean", "wait_std", "Average Waiting Time (s)")]:
    fig, axes = plt.subplots(5, 2, figsize=(16, 20))
    width = 0.25
    for ax, tasks in zip(axes.flat, task_counts):
        subset = df[df.tasks == tasks]
        for k, (w1, w2) in enumerate([(0.5, 0.5), (0.7, 0.3), (0.3, 0.7)]):
            d = subset[(subset.w1 == w1) & (subset.w2 == w2)].set_index("algo").loc[order]
            ax.bar([i + k * width for i in range(len(order))], d[metric], width, yerr=d[err], capsize=3, label=f"w1={w1}, w2={w2}")
        ax.set_xticks([i + width for i in range(len(order))])
        ax.set_xticklabels(order)
        ax.set_title(f"{title} - {tasks} task")
        ax.legend()
    plt.tight_layout()
    plt.savefig(f"{metric}.png", dpi=200)
    plt.close(fig)
print("Grafik 100-1000 task selesai.")
