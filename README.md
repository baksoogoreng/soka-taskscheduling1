# Optimasi Penjadwalan Task pada Komputasi Awan (CloudSim)

Simulasi dan analisis perbandingan lima algoritma penjadwalan task (Round Robin, ABC, Cuckoo Search, hybrid RR-ABC, dan hybrid GA-GWO) pada lingkungan cloud heterogen menggunakan CloudSim 7.0.1.

**Kelompok 5 — Institut Teknologi Sepuluh Nopember**

| NRP | Nama |
|---|---|
| 5027241009 | Tasya Aulia Darmawan |
| 5027241043 | Azaria Raissa Maulidinnisa |
| 5027241090 | Tiara Fatimah Azzahra |
| 5027241104 | Afriza Tristan Calendra Rajasa |
| 5027201047 | Made Gde Krisna Wangsa |

---

## Daftar Isi

1. [Ringkasan Pendekatan](#ringkasan-pendekatan)
2. [Dataset dan Model Task](#dataset-dan-model-task)
3. [Desain Arsitektur Cloud](#desain-arsitektur-cloud)
4. [Fungsi Objektif dan Metrik](#fungsi-objektif-dan-metrik)
5. [Algoritma yang Diuji](#algoritma-yang-diuji)
6. [Protokol Eksperimen](#protokol-eksperimen)
7. [Hasil dan Analisis](#hasil-dan-analisis)
8. [Kesimpulan](#kesimpulan)
9. [Struktur Project](#struktur-project)
10. [Referensi](#referensi)

---

## Ringkasan Pendekatan

Berdasarkan telaah tiga jurnal *systematic literature review* (Khan et al., 2023; Abraham et al., 2024; Sanjalawe et al., 2025), riset task scheduling di cloud computing berevolusi dari algoritma heuristik konvensional menuju metaheuristik yang lebih fleksibel, hingga pendekatan hybrid yang menggabungkan kekuatan beberapa metode. Proyek ini mengikuti alur evolusi yang sama, dibagi tiga tahap:

| Tahap | Fokus | Algoritma |
|---|---|---|
| 1 | Baseline heuristik | Round Robin (RR) |
| 2 | Metaheuristik independen | Artificial Bee Colony (ABC), Cuckoo Search (CS) |
| 3 | Hybrid | RR-ABC (heuristik + metaheuristik), GA-GWO (metaheuristik + metaheuristik) |

## Dataset dan Model Task

- **Model task:** independent, non-preemptive, statis (semua task tersedia sejak awal simulasi).
- **Dataset:** [GoCJ (Google Cloud Jobs)](https://data.mendeley.com/datasets/b7bp6xhrcd/1) (Hussain et al., 2019), hasil pemodelan Monte Carlo dari Google cluster traces.
- **Dataset eksperimen:** file GoCJ dengan 100–1000 job (kelipatan 100), nilai MI (Million Instructions) dipetakan langsung sebagai `cloudlet length`.

## Desain Arsitektur Cloud

| Komponen | Spesifikasi | Jumlah |
|---|---|---|
| Datacenter | Satu datacenter tunggal | 1 |
| Host | Heterogen, 2 tipe | 4 (2 Host A + 2 Host B) |
| VM | Heterogen, 2 tipe | 8 (4 kecil + 4 besar) |
| Task/Cloudlet | Independent, non-preemptive | 100–1000 cloudlet |

**Host:**

| Tipe | PE | MIPS | RAM | Bandwidth | Jumlah |
|---|---|---|---|---|---|
| A | 1 | 1000 | 2048 MB | 10000 | 2 |
| B | 1 | 2000 | 4096 MB | 10000 | 2 |

**VM:**

| Tipe | PE | MIPS | RAM | Jumlah |
|---|---|---|---|---|
| Kecil | 1 | 500 | 512 MB | 4 |
| Besar | 1 | 1000 | 1024 MB | 4 |

**Batasan (constraints):**
- Kapasitas VM tidak boleh dilampaui — dikelola otomatis oleh `VmSchedulerTimeShared` di CloudSim.
- Non-preemptive — diimplementasikan lewat `CloudletSchedulerSpaceShared`.
- Eksklusivitas eksekusi — satu task hanya dijalankan pada satu VM.

## Fungsi Objektif dan Metrik

Dua objektif digabungkan dengan pendekatan *weighted-sum*:

```
Fitness = w1 × makespan_normalisasi − w2 × utilization_normalisasi   (w1 + w2 = 1)
```

| Objektif | Arah | Metrik pendukung |
|---|---|---|
| Minimasi Makespan | ↓ | Average Waiting Time |
| Maksimasi Resource Utilization | ↑ | — |

Tiga skenario bobot diuji untuk *sensitivity analysis*:

| Skenario | w1 (makespan) | w2 (utilization) |
|---|---|---|
| Seimbang | 0.5 | 0.5 |
| Prioritas Kecepatan | 0.7 | 0.3 |
| Prioritas Efisiensi Resource | 0.3 | 0.7 |

## Algoritma yang Diuji

| Algoritma | Jenis | Catatan |
|---|---|---|
| Round Robin (RR) | Heuristik | Baseline, deterministik (3 run) |
| Artificial Bee Colony (ABC) | Metaheuristik | Kuat di *exploitation* |
| Cuckoo Search (CS) | Metaheuristik | Kuat di *exploration* (Lévy flight) |
| RR-ABC | Hybrid heuristik + metaheuristik | Sebagian populasi awal ABC diisi solusi RR |
| GA-GWO | Hybrid metaheuristik + metaheuristik | Fase GWO (eksploitasi alpha/beta/delta) + fase GA (seleksi, crossover, mutasi) |

Parameter: populasi 30, iterasi 100 (disamakan antar algoritma untuk kewajaran perbandingan).

## Protokol Eksperimen

- Jumlah task: **100, 200, 300, 400, 500, 600, 700, 800, 900, 1000**, menggunakan file GoCJ sesuai ukurannya; ukuran kelipatan 50 dilewati.
- Semua algoritma (termasuk RR) dijalankan **3 kali** untuk setiap jumlah task dan skenario bobot, dengan seed 1000–1002.
- Total **450 hasil run**: 10 ukuran × 5 algoritma × 3 bobot × 3 pengulangan.
- Output terminal menampilkan tabel tiap run dan ringkasan **rata-rata ± standar deviasi**.
- `results.csv` menyimpan tiap run; `summary.csv` menyimpan 150 ringkasan, dengan kolom `tasks` untuk membedakan ukuran dataset.
- Validasi CloudSim dijalankan pada pengulangan terakhir tiap algoritma untuk bobot 0.5/0.5 di setiap ukuran.
- Simulasi terbaru telah selesai: 450 run, 150 ringkasan, dan 50 validasi CloudSim cocok dengan hasil analitis. Output rapi tersedia di `cloudsim-soka/simulation-output.txt`.
- Grafik, tabel, pembahasan, dan kesimpulan di bawah menggunakan eksperimen terbaru (100–1000 task, 3 run). Standar deviasi menggunakan rumus sampel (`n - 1`). RR diulang untuk keseragaman protokol; pengulangannya identik karena deterministik.

## Hasil dan Analisis

### Makespan

![Makespan](cloudsim-soka/mk_mean.png)

### Resource Utilization

![Resource Utilization](cloudsim-soka/util_mean.png)

### Average Waiting Time

![Average Waiting Time](cloudsim-soka/wait_mean.png)

### Ringkasan Makespan pada Semua Ukuran (w1=0.5, w2=0.5)

Nilai dalam detik, berupa rata-rata ± standar deviasi dari 3 run.

| Task | RR | ABC | CS | RR-ABC | GA-GWO |
|---|---|---|---|---|---|
| 100 | 4513.00 ± 0.00 | 2390.50 ± 13.89 | 2321.50 ± 9.58 | 2395.00 ± 31.00 | 2419.00 ± 27.53 |
| 200 | 10523.00 ± 0.00 | 4772.83 ± 18.37 | 4689.00 ± 22.15 | 4879.00 ± 19.05 | 4982.67 ± 26.08 |
| 300 | 13278.00 ± 0.00 | 7399.83 ± 186.12 | 7143.50 ± 39.40 | 7272.67 ± 64.29 | 7756.00 ± 180.93 |
| 400 | 13233.00 ± 0.00 | 9129.00 ± 285.93 | 9003.83 ± 108.31 | 9160.83 ± 98.64 | 10180.33 ± 864.24 |
| 500 | 18954.00 ± 0.00 | 11809.67 ± 135.80 | 11270.33 ± 122.00 | 11902.00 ± 255.69 | 13376.00 ± 968.59 |
| 600 | 21334.00 ± 0.00 | 14998.17 ± 53.02 | 14274.00 ± 58.20 | 14894.83 ± 88.91 | 18386.33 ± 1152.89 |
| 700 | 28424.00 ± 0.00 | 16128.67 ± 403.99 | 15170.00 ± 76.10 | 16160.83 ± 60.97 | 19734.17 ± 915.95 |
| 800 | 28162.00 ± 0.00 | 18058.17 ± 375.44 | 17383.50 ± 362.65 | 18384.83 ± 947.31 | 23728.00 ± 1018.95 |
| 900 | 31776.00 ± 0.00 | 22811.83 ± 558.43 | 20900.83 ± 248.09 | 22159.50 ± 439.49 | 29000.67 ± 1317.48 |
| 1000 | 36583.00 ± 0.00 | 25058.67 ± 449.06 | 22655.67 ± 222.30 | 24496.33 ± 786.97 | 31012.67 ± 1410.12 |

### Ringkasan 1000 Task — Seimbang (w1=0.5)

| Algoritma | Makespan (s) | Utilization (%) | Waiting Time (s) | Perbaikan makespan vs RR |
|---|---|---|---|---|
| RR | 36583.00 ± 0.00 | 66.83 ± 0.00 | 11876.37 ± 0.00 | 0.00% |
| ABC | 25058.67 ± 449.06 | 89.14 ± 1.32 | 10773.99 ± 72.55 | 31.50% |
| CS | 22655.67 ± 222.30 | 95.78 ± 0.71 | 10515.06 ± 79.70 | 38.07% |
| RR-ABC | 24496.33 ± 786.97 | 90.50 ± 2.54 | 10646.65 ± 36.21 | 33.04% |
| GA-GWO | 31012.67 ± 1410.12 | 76.48 ± 1.96 | 11557.97 ± 129.58 | 15.23% |

### Ringkasan 1000 Task — Prioritas Kecepatan (w1=0.7)

| Algoritma | Makespan (s) | Utilization (%) | Waiting Time (s) | Perbaikan makespan vs RR |
|---|---|---|---|---|
| RR | 36583.00 ± 0.00 | 66.83 ± 0.00 | 11876.37 ± 0.00 | 0.00% |
| ABC | 24149.67 ± 731.03 | 91.61 ± 2.44 | 10582.90 ± 67.84 | 33.99% |
| CS | 23306.00 ± 325.57 | 94.09 ± 0.88 | 10644.45 ± 30.33 | 36.29% |
| RR-ABC | 25144.00 ± 501.88 | 89.19 ± 1.17 | 10823.57 ± 150.51 | 31.27% |
| GA-GWO | 29960.67 ± 725.36 | 77.71 ± 0.82 | 11952.77 ± 805.50 | 18.10% |

### Ringkasan 1000 Task — Prioritas Efisiensi Resource (w1=0.3)

| Algoritma | Makespan (s) | Utilization (%) | Waiting Time (s) | Perbaikan makespan vs RR |
|---|---|---|---|---|
| RR | 36583.00 ± 0.00 | 66.83 ± 0.00 | 11876.37 ± 0.00 | 0.00% |
| ABC | 25307.33 ± 468.04 | 88.52 ± 1.20 | 10811.65 ± 72.49 | 30.82% |
| CS | 23113.67 ± 584.95 | 94.28 ± 2.00 | 10512.17 ± 5.43 | 36.82% |
| RR-ABC | 25356.67 ± 263.51 | 88.21 ± 0.39 | 10750.99 ± 39.34 | 30.69% |
| GA-GWO | 31012.67 ± 1410.12 | 76.48 ± 1.96 | 11557.97 ± 129.58 | 15.23% |

### Pembahasan

- **CS menghasilkan makespan rata-rata terendah pada seluruh 30 kombinasi ukuran dan bobot.** Perbaikan terhadap RR berkisar 31.87–55.55%, dengan utilization 94.03–96.74%. Pada 1000 task dan bobot seimbang, makespan turun dari 36583.00 s menjadi 22655.67 s (38.07%).
- **ABC dan RR-ABC kompetitif, tetapi keunggulan hybrid bergantung pada ukuran dan bobot.** Pada 1000 task dengan bobot seimbang, RR-ABC lebih cepat daripada ABC (24496.33 vs 25058.67 s); pada bobot 0.7/0.3, ABC lebih cepat (24149.67 vs 25144.00 s).
- **GA-GWO tertinggal pada ukuran besar dengan parameter yang digunakan.** Pada 1000 task dan bobot seimbang, utilization-nya 76.48% dan perbaikan makespan 15.23%. Ini merupakan hasil konfigurasi eksperimen ini, bukan kesimpulan umum tentang algoritma.
- **Waiting time perlu dinilai terpisah dari makespan.** Sebagai contoh, pada 100 task dan bobot seimbang, ABC memiliki waiting time lebih rendah daripada CS (976.81 vs 995.55 s), meskipun CS memiliki makespan lebih rendah.
- **50 validasi CloudSim dalam log menunjukkan seluruh task selesai dan makespan cocok dengan perhitungan analitis hingga presisi yang dicetak.** Validasi dilakukan pada run terakhir, bobot 0.5/0.5, untuk lima algoritma di sepuluh ukuran. Utilization dan waiting time dalam CSV dihitung secara analitis; log tidak memvalidasi kedua metrik tersebut secara langsung.

Data lengkap: [450 hasil run](cloudsim-soka/results.csv), [150 ringkasan](cloudsim-soka/summary.csv), dan [log simulasi](cloudsim-soka/simulation-output.txt).

## Kesimpulan

Pada eksperimen GoCJ 100–1000 task dengan 4 host, 8 VM, populasi 30, dan 100 iterasi, **Cuckoo Search memberikan makespan rata-rata terbaik pada semua ukuran dan skenario bobot yang diuji**. ABC dan RR-ABC berada di bawah CS dengan urutan yang berubah antar konfigurasi, sedangkan GA-GWO mengalami penurunan kualitas relatif pada ukuran besar.

Hasil berasal dari **3 run per kombinasi**, sehingga belum cukup untuk menyatakan signifikansi statistik atau keunggulan universal. Setiap ukuran menggunakan file dataset GoCJ tersendiri; perubahan hasil antar ukuran juga dipengaruhi komposisi panjang task, bukan hanya jumlah task. Hasil lama 40 task tetap dapat ditelusuri melalui riwayat Git.

## Struktur Project

```
cloudsim-soka/
├── pom.xml
├── results.csv          # hasil tiap run (450 run: 10 ukuran × 5 algoritma × 3 bobot × 3 run)
├── summary.csv           # 150 ringkasan per ukuran, algoritma, dan bobot
├── mk_mean.png            # grafik makespan
├── util_mean.png          # grafik resource utilization
├── wait_mean.png          # grafik average waiting time
├── analyze.py             # skrip olah data (pandas + matplotlib)
└── src/main/
    ├── java/soka/
    │   ├── Main.java          # entry point, jalankan seluruh eksperimen
    │   ├── CloudEnv.java      # setup datacenter, host, VM (CloudSim)
    │   ├── DatasetLoader.java # loader dataset GoCJ
    │   ├── Metrics.java       # makespan, utilization, waiting time, fitness
    │   └── algo/
    │       ├── Scheduler.java     # interface algoritma
    │       ├── RoundRobin.java
    │       ├── ABC.java
    │       ├── CuckooSearch.java
    │       ├── RRABC.java
    │       └── GAGWO.java
    └── resources/
        └── gocj_sample.txt    # 40 baris sampel dataset GoCJ
```

### Cara Menjalankan

Dataset aktif dibaca dari `GoCJ Google Cloud Jobs Dataset/GoCJ_Dataset_<jumlah>.txt` pada root repositori. `gocj_sample.txt` merupakan sampel historis dan tidak digunakan oleh eksperimen terbaru. Jalankan dari direktori `cloudsim-soka`; eksekusi simulasi akan menulis ulang `results.csv` dan `summary.csv`.

Memerlukan **JDK 21** (sesuai CloudSim 7.0.1) dan Maven. Dependensi CloudSim 7.0.1 perlu dibangun dari tag resmi `7.0.1` repositori Cloudslab/cloudsim, lalu diinstal ke repositori Maven lokal karena belum tersedia di Maven Central.

```bash
cd cloudsim-soka
mvn compile exec:java -Dexec.mainClass=soka.Main
python -m pip install pandas matplotlib
python analyze.py
```

## Referensi

- Khan, Z. A., Aziz, I. A., Osman, N. A. B., & Ullah, I. (2023). A Review on Task Scheduling Techniques in Cloud and Fog Computing. *IEEE Access*, 11, 143417–143445.
- Abraham, O. L., Ngadi, M. A. B., Sharif, J. B. M., & Sidik, M. K. M. (2024). Task Scheduling in Cloud Environment. *IEEE Access*, 12, 138252–138279.
- Sanjalawe, Y., Al-E'mari, S., Fraihat, S., & Makhadmeh, S. (2025). AI-driven Job Scheduling in Cloud Computing. *Artificial Intelligence Review*, 58, Art. 197.
- Hussain, A., Aleem, M., Khan, A., Iqbal, M. A., & Islam, M. A. (2019). GoCJ: Google Cloud Jobs Dataset. *Data*, 4(38).
- Alhaidari, F., & Balharith, T. Z. (2021). Enhanced Round-Robin Algorithm in the Cloud Computing Environment. *Computers*, 10(5), 63.
- Tawfik, M. A., Bahgat, A., Keshk, A., & Torkey, F. A. (2015). Artificial Bee Colony Algorithm for Cloud Task Scheduling. *IJCI*, 4(1), 1–10.
- Agarwal, M., & Srivastava, G. M. S. (2018). A Cuckoo Search Algorithm-Based Task Scheduling in Cloud Computing. *AISC*, Vol. 554, Springer.
- Kruekaew, B., & Kimpan, W. (2020). Enhancing of Artificial Bee Colony Algorithm for VM Scheduling and Load Balancing. *IJCIS*, 13(1), 496–510.
- Behera, I., & Sobhanayak, S. (2024). Task Scheduling Optimization in Heterogeneous Cloud Computing Environments: A Hybrid GA-GWO Approach. *JPDC*, 183, 104766.