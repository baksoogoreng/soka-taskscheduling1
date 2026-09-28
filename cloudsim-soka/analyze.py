import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv("summary.csv")
order = ["RR", "ABC", "CS", "RR-ABC", "GA-GWO"]

for metric, err, title in [("mk_mean", "mk_std", "Makespan (s)"),
                           ("util_mean", "util_std", "Resource Utilization (%)"),
                           ("wait_mean", "wait_std", "Average Waiting Time (s)")]:
    fig, ax = plt.subplots(figsize=(8, 4))
    width = 0.25
    for k, (w1, w2) in enumerate([(0.5, 0.5), (0.7, 0.3), (0.3, 0.7)]):
        d = df[(df.w1 == w1) & (df.w2 == w2)].set_index("algo").loc[order]
        ax.bar([i + k * width for i in range(len(order))], d[metric],
               width, yerr=d[err], capsize=3, label=f"w1={w1}, w2={w2}")
    ax.set_xticks([i + width for i in range(len(order))])
    ax.set_xticklabels(order)
    ax.set_title(title)
    ax.legend()
    plt.tight_layout()
    plt.savefig(f"{metric}.png", dpi=200)

print(df.round(3).to_string(index=False))