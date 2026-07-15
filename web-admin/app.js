import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.5/firebase-app.js";
import {
  addDoc,
  collection,
  doc,
  getDocs,
  getFirestore,
  orderBy,
  query,
  serverTimestamp,
  updateDoc,
} from "https://www.gstatic.com/firebasejs/10.12.5/firebase-firestore.js";

const firebaseConfig = {
  apiKey: "AIzaSyDKFfd_8n5jJwy1wN__XigSvZvuv_pmwwQ",
  authDomain: "xarays-6470b.firebaseapp.com",
  databaseURL: "https://xarays-6470b-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "xarays-6470b",
  storageBucket: "xarays-6470b.firebasestorage.app",
  messagingSenderId: "574336098762",
  appId: "1:574336098762:web:e9404294715b20d2b36f33",
  measurementId: "G-KGCS16KPF9",
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);
const productsRef = collection(db, "products");
const ordersRef = collection(db, "orders");
const chatsRef = collection(db, "chats");

const form = document.getElementById("productForm");
const productStatusEl = document.getElementById("productStatus");
const productListEl = document.getElementById("productList");
const orderStatusEl = document.getElementById("orderStatus");
const orderListEl = document.getElementById("orderList");
const chatStatusEl = document.getElementById("chatStatus");
const chatListEl = document.getElementById("chatList");
const chatOrderIdSelect = document.getElementById("chatOrderId");
const chatSenderNameInput = document.getElementById("chatSenderName");
const chatMessageInput = document.getElementById("chatMessage");
const sendChatBtn = document.getElementById("sendChatBtn");
const reloadProductsBtn = document.getElementById("reloadProductsBtn");
const reloadOrdersBtn = document.getElementById("reloadOrdersBtn");
const reloadChatBtn = document.getElementById("reloadChatBtn");
const seedBtn = document.getElementById("seedBtn");
const typeSelect = document.getElementById("type");
const accountCredentials = document.getElementById("accountCredentials");
const accountLoginInput = document.getElementById("accountLogin");
const accountPasswordInput = document.getElementById("accountPassword");
const orderSortSelect = document.getElementById("orderSort");
const orderStatusFilterSelect = document.getElementById("orderStatusFilter");
const tabs = Array.from(document.querySelectorAll(".tab"));
const panels = {
  products: document.getElementById("panel-products"),
  orders: document.getElementById("panel-orders"),
  chat: document.getElementById("panel-chat"),
};

function setStatus(el, message) {
  el.textContent = message;
}

function formatPrice(value) {
  return new Intl.NumberFormat("id-ID").format(Number(value || 0));
}

function formatDate(value) {
  if (!value) return "-";
  const date = value.toDate ? value.toDate() : new Date(value);
  return date.toLocaleString("id-ID");
}

function readFormValue(id) {
  return document.getElementById(id).value.trim();
}

function statusLabel(value) {
  const labels = {
    BATAL: "Batal",
    SELESAI: "Selesai",
    MENUNGGU_PEMBAYARAN_MASUK: "Menunggu Pembayaran Masuk",
    PESANAN_DIPROSES: "Pesanan Diproses",
  };

  return labels[value] || value || "-";
}

function normalizeOrderStatus(value) {
  return (value || "").trim().toUpperCase();
}

function compareOrderDates(a, b, direction) {
  const left = Number(a.orderDate || 0);
  const right = Number(b.orderDate || 0);
  return direction === "oldest" ? left - right : right - left;
}

function filterAndSortOrders(orders) {
  const filtered = orders.filter((order) => {
    const selectedStatus = orderStatusFilterSelect.value;
    if (selectedStatus === "ALL") {
      return true;
    }
    return normalizeOrderStatus(order.status) === selectedStatus;
  });

  return filtered.sort((a, b) => compareOrderDates(a, b, orderSortSelect.value));
}

function toggleAccountFields() {
  const isAccount = typeSelect.value === "ACCOUNT";
  accountCredentials.classList.toggle("hidden", !isAccount);
  accountLoginInput.required = isAccount;
  accountPasswordInput.required = isAccount;
}

function switchTab(name) {
  tabs.forEach((tab) => tab.classList.toggle("active", tab.dataset.tab === name));
  Object.entries(panels).forEach(([key, panel]) => {
    panel.classList.toggle("active", key === name);
  });
}

tabs.forEach((tab) => {
  tab.addEventListener("click", () => switchTab(tab.dataset.tab));
});

typeSelect.addEventListener("change", toggleAccountFields);
toggleAccountFields();

async function loadProducts() {
  productListEl.innerHTML = "";
  setStatus(productStatusEl, "Memuat produk...");

  const snapshot = await getDocs(query(productsRef, orderBy("createdAt", "desc")));

  if (snapshot.empty) {
    productListEl.innerHTML = `<div class="item muted">Belum ada data produk.</div>`;
    setStatus(productStatusEl, "Data kosong.");
    return;
  }

  snapshot.forEach((item) => {
    const data = item.data();
    const row = document.createElement("div");
    row.className = "item";
    row.innerHTML = `
      <h3>${data.title || "(tanpa judul)"}</h3>
      <div class="meta">
        <span><strong>Game:</strong> ${data.game || "-"}</span>
        <span><strong>Harga:</strong> Rp ${formatPrice(data.price)}</span>
        <span><strong>Tipe:</strong> ${data.type || "-"}</span>
        <span><strong>Seller:</strong> ${data.sellerId || "-"}</span>
        <span><strong>Stok:</strong> ${Number(data.stock || 0)}</span>
        <span><strong>Created:</strong> ${formatDate(data.createdAt)}</span>
      </div>
      ${
        data.type === "ACCOUNT"
          ? `<div class="meta">
              <span><strong>Login:</strong> ${data.accountLogin || "-"}</span>
              <span><strong>Password:</strong> ${data.accountPassword || "-"}</span>
            </div>`
          : ""
      }
      <p class="muted">${data.description || ""}</p>
      <div class="order-proof">
        <label>Ubah stok
          <input data-stock-input="${item.id}" type="number" min="0" step="1" value="${Number(data.stock || 0)}" />
        </label>
        <button data-save-stock="${item.id}" class="secondary">Simpan Stok</button>
      </div>
    `;
    productListEl.appendChild(row);
  });

  productListEl.querySelectorAll("[data-save-stock]").forEach((button) => {
    button.addEventListener("click", async () => {
      const id = button.dataset.saveStock;
      const stockInput = productListEl.querySelector(`[data-stock-input="${id}"]`);
      const stock = Number(stockInput.value);

      if (Number.isNaN(stock) || stock < 0) {
        setStatus(productStatusEl, "Stok harus angka 0 atau lebih.");
        return;
      }

      try {
        button.disabled = true;
        button.textContent = "Menyimpan...";
        await updateDoc(doc(db, "products", id), {
          stock,
        });
        setStatus(productStatusEl, "Stok produk berhasil diperbarui.");
        await loadProducts();
      } catch (error) {
        console.error(error);
        setStatus(productStatusEl, `Gagal memperbarui stok: ${error.message}`);
      } finally {
        button.disabled = false;
        button.textContent = "Simpan Stok";
      }
    });
  });

  setStatus(productStatusEl, `Berhasil memuat ${snapshot.size} produk.`);
}

async function loadOrders() {
  orderListEl.innerHTML = "";
  setStatus(orderStatusEl, "Memuat pesanan...");

  const snapshot = await getDocs(query(ordersRef, orderBy("orderDate", "desc")));

  if (snapshot.empty) {
    orderListEl.innerHTML = `<div class="item muted">Belum ada pesanan.</div>`;
    setStatus(orderStatusEl, "Data kosong.");
    return;
  }

  const orders = snapshot.docs.map((item) => ({
    id: item.id,
    ...item.data(),
  }));
  const visibleOrders = filterAndSortOrders(orders);

  visibleOrders.forEach((data) => {
    const row = document.createElement("div");
    row.className = "item order-item";
    row.innerHTML = `
      <div class="order-header">
        <div>
          <h3>${data.productTitle || "(tanpa judul)"}</h3>
          <div class="meta">
            <span><strong>Pembeli:</strong> ${data.buyerName || "-"}</span>
            <span><strong>Email:</strong> ${data.buyerEmail || "-"}</span>
            <span><strong>WA:</strong> ${data.buyerPhone || "-"}</span>
            <span><strong>Status:</strong> ${statusLabel(data.status)}</span>
          </div>
      </div>
        <button class="secondary" data-open-order="${data.id}">Update</button>
      </div>
      <p class="muted">${data.additionalInfo || ""}</p>
      <div class="meta">
        <span><strong>Metode:</strong> ${data.paymentMethod || "-"}</span>
        <span><strong>Total:</strong> Rp ${formatPrice(data.totalAmount)}</span>
        <span><strong>Order:</strong> ${formatDate(data.orderDate)}</span>
      </div>
      <div class="order-proof">
        <label>Update status
          <select data-status-input="${data.id}">
            <option value="MENUNGGU_PEMBAYARAN_MASUK" ${data.status === "MENUNGGU_PEMBAYARAN_MASUK" ? "selected" : ""}>Menunggu Pembayaran Masuk</option>
            <option value="PESANAN_DIPROSES" ${data.status === "PESANAN_DIPROSES" ? "selected" : ""}>Pesanan Diproses</option>
            <option value="SELESAI" ${data.status === "SELESAI" ? "selected" : ""}>Selesai</option>
            <option value="BATAL" ${data.status === "BATAL" ? "selected" : ""}>Batal</option>
          </select>
        </label>
        <label>Link bukti / resi
          <input data-proof-input="${data.id}" type="url" value="${data.proofUrl || ""}" placeholder="https://..." />
        </label>
        <label>Balasan customer
          <textarea data-reply-input="${data.id}" rows="3" placeholder="Tulis pesan ke customer...">${data.adminReply || ""}</textarea>
        </label>
        <button data-save-order="${data.id}">Simpan Bukti</button>
      </div>
    `;
    orderListEl.appendChild(row);
  });

  chatOrderIdSelect.innerHTML = `<option value="">Chat umum</option>`;
  visibleOrders.forEach((order) => {
    const option = document.createElement("option");
    option.value = order.id;
    option.textContent = `${order.productTitle || "(tanpa judul)"} - ${order.buyerName || "-"}`;
    chatOrderIdSelect.appendChild(option);
  });

  orderListEl.querySelectorAll("[data-save-order]").forEach((button) => {
    button.addEventListener("click", async () => {
      const id = button.dataset.saveOrder;
      const status = orderListEl.querySelector(`[data-status-input="${id}"]`).value.trim();
      const proofUrl = orderListEl.querySelector(`[data-proof-input="${id}"]`).value.trim();
      const adminReply = orderListEl.querySelector(`[data-reply-input="${id}"]`).value.trim();

      try {
        button.disabled = true;
        button.textContent = "Menyimpan...";
        await updateDoc(doc(db, "orders", id), {
          status,
          proofUrl,
          adminReply,
          updatedAt: Date.now(),
        });
        setStatus(orderStatusEl, "Pesanan berhasil diperbarui.");
        await loadOrders();
      } catch (error) {
        console.error(error);
        setStatus(orderStatusEl, `Gagal memperbarui pesanan: ${error.message}`);
      } finally {
        button.disabled = false;
        button.textContent = "Simpan Bukti";
      }
    });
  });

  setStatus(orderStatusEl, `Berhasil memuat ${visibleOrders.length} pesanan.`);
}

async function loadChat() {
  chatListEl.innerHTML = "";
  setStatus(chatStatusEl, "Memuat chat...");

  const snapshot = await getDocs(query(chatsRef, orderBy("createdAt", "desc")));

  if (snapshot.empty) {
    chatListEl.innerHTML = `<div class="item muted">Belum ada chat.</div>`;
    setStatus(chatStatusEl, "Data kosong.");
    return;
  }

  snapshot.forEach((item) => {
    const data = item.data();
    const row = document.createElement("div");
    row.className = "item";
    row.innerHTML = `
      <h3>${data.senderName || "-"}</h3>
      <div class="meta">
        <span><strong>Sender:</strong> ${data.senderId || "-"}</span>
        <span><strong>Order:</strong> ${data.orderId || "-"}</span>
        <span><strong>Waktu:</strong> ${formatDate(data.createdAt)}</span>
      </div>
      <p>${data.message || ""}</p>
    `;
    chatListEl.appendChild(row);
  });

  setStatus(chatStatusEl, `Berhasil memuat ${snapshot.size} chat.`);
}

async function sendAdminChat() {
  const message = chatMessageInput.value.trim();
  const senderName = chatSenderNameInput.value.trim() || "Admin";
  const orderId = chatOrderIdSelect.value.trim();

  if (!message) {
    setStatus(chatStatusEl, "Pesan chat belum diisi.");
    return;
  }

  try {
    sendChatBtn.disabled = true;
    sendChatBtn.textContent = "Mengirim...";
    setStatus(chatStatusEl, "Mengirim chat...");
    await addDoc(chatsRef, {
      senderId: "admin",
      senderName,
      message,
      orderId: orderId || null,
      createdAt: Date.now(),
    });
    chatMessageInput.value = "";
    chatOrderIdSelect.value = "";
    setStatus(chatStatusEl, "Chat admin berhasil dikirim.");
    await loadChat();
  } catch (error) {
    console.error(error);
    setStatus(chatStatusEl, `Gagal mengirim chat: ${error.message}`);
  } finally {
    sendChatBtn.disabled = false;
    sendChatBtn.textContent = "Kirim Chat";
  }
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const payload = {
    title: readFormValue("title"),
    game: readFormValue("game"),
    price: Number(readFormValue("price")),
    description: readFormValue("description"),
    type: document.getElementById("type").value,
    sellerId: readFormValue("sellerId") || "admin",
    imageUrl: readFormValue("imageUrl") || "",
    stock: Number(readFormValue("stock")),
    createdAt: Date.now(),
    accountLogin: accountLoginInput.value.trim(),
    accountPassword: accountPasswordInput.value.trim(),
  };

  if (!payload.title || !payload.game || !payload.description || Number.isNaN(payload.price)) {
    setStatus(productStatusEl, "Mohon lengkapi semua field dengan benar.");
    return;
  }

  if (Number.isNaN(payload.stock) || payload.stock < 0) {
    setStatus(productStatusEl, "Stok harus angka 0 atau lebih.");
    return;
  }

  if (payload.type === "ACCOUNT" && (!payload.accountLogin || !payload.accountPassword)) {
    setStatus(productStatusEl, "Untuk kategori ACCOUNT, email/username login dan password wajib diisi.");
    return;
  }

  if (payload.type !== "ACCOUNT") {
    payload.accountLogin = "";
    payload.accountPassword = "";
  }

  try {
    setStatus(productStatusEl, "Menyimpan data...");
    await addDoc(productsRef, payload);
    form.reset();
    document.getElementById("sellerId").value = "admin";
    document.getElementById("stock").value = "1";
    toggleAccountFields();
    setStatus(productStatusEl, "Data berhasil disimpan ke Firestore.");
    await loadProducts();
  } catch (error) {
    console.error(error);
    setStatus(productStatusEl, `Gagal menyimpan: ${error.message}`);
  }
});

reloadProductsBtn.addEventListener("click", () => loadProducts().catch((error) => setStatus(productStatusEl, `Gagal memuat data: ${error.message}`)));
reloadOrdersBtn.addEventListener("click", () => loadOrders().catch((error) => setStatus(orderStatusEl, `Gagal memuat data: ${error.message}`)));
reloadChatBtn.addEventListener("click", () => loadChat().catch((error) => setStatus(chatStatusEl, `Gagal memuat data: ${error.message}`)));
orderSortSelect.addEventListener("change", () => loadOrders().catch((error) => setStatus(orderStatusEl, `Gagal memuat data: ${error.message}`)));
orderStatusFilterSelect.addEventListener("change", () => loadOrders().catch((error) => setStatus(orderStatusEl, `Gagal memuat data: ${error.message}`)));
sendChatBtn.addEventListener("click", () => sendAdminChat());

seedBtn.addEventListener("click", async () => {
  const samples = [
    {
      title: "Akun Mobile Legends Mythic",
      game: "Mobile Legends",
      price: 500000,
      description: "Akun ML rank Mythic, cocok untuk push rank.",
      type: "ACCOUNT",
      sellerId: "admin",
      imageUrl: "",
      stock: 1,
      createdAt: Date.now(),
    },
    {
      title: "Top Up Free Fire 1000 Diamond",
      game: "Free Fire",
      price: 150000,
      description: "Diamond FF langsung masuk.",
      type: "TOPUP",
      sellerId: "admin",
      imageUrl: "",
      stock: 10,
      createdAt: Date.now(),
    },
  ];

  try {
    setStatus(productStatusEl, "Menyimpan contoh data...");
    for (const sample of samples) {
      await addDoc(productsRef, sample);
    }
    setStatus(productStatusEl, "Contoh data berhasil ditambahkan.");
    await loadProducts();
  } catch (error) {
    console.error(error);
    setStatus(productStatusEl, `Gagal menambah contoh data: ${error.message}`);
  }
});

loadProducts().catch((error) => setStatus(productStatusEl, `Gagal memuat data: ${error.message}`));
loadOrders().catch((error) => setStatus(orderStatusEl, `Gagal memuat data: ${error.message}`));
loadChat().catch((error) => setStatus(chatStatusEl, `Gagal memuat data: ${error.message}`));
