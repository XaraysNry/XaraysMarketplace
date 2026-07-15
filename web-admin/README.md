# Xarays Marketplace Web Admin

Dashboard ringan untuk input produk ke Firebase Firestore.

## Cara pakai

1. Buka `web-admin/app.js`.
2. Isi `firebaseConfig` dengan konfigurasi project Firebase kamu.
3. Jalankan folder ini lewat web server lokal, bukan `file://`.

Contoh paling mudah:

```bash
cd web-admin
npx serve
```

Atau pakai extension Live Server di VS Code.

## Collection yang dipakai

`products`

## Field yang disimpan

- `title`
- `game`
- `price`
- `description`
- `type`
- `sellerId`
- `imageUrl` opsional
- `createdAt`

## Catatan

App Android kamu saat ini membaca `game` untuk menentukan gambar kategori di mobile. Jadi kalau isi `game` sesuai nama game yang sudah dikenali, tampilannya langsung sinkron.
