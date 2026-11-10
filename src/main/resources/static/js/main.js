document.addEventListener('DOMContentLoaded', function () {
    // Auto dismiss alert messages after 5 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            try {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            } catch (e) {
                // Ignore if already dismissed
            }
        }, 5000);
    });

    // Handle AJAX Add to Cart for all product forms
    document.addEventListener('submit', function (e) {
        const form = e.target.closest('form');
        if (!form) return;

        const action = form.getAttribute('action') || '';
        if (!action.includes('/cart/add')) {
            return;
        }

        // Prevent default navigation / redirect
        e.preventDefault();

        const submitBtn = form.querySelector('button[type="submit"]');
        const originalBtnHtml = submitBtn ? submitBtn.innerHTML : null;
        const hadOutline = submitBtn && submitBtn.classList.contains('btn-outline-success');

        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span> Đang thêm...';
        }

        const formData = new FormData(form);

        // Fetch CSRF token if available in meta tags
        const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
        const headers = {
            'X-Requested-With': 'XMLHttpRequest',
            'Accept': 'application/json'
        };
        if (csrfTokenMeta && csrfHeaderMeta) {
            headers[csrfHeaderMeta.getAttribute('content')] = csrfTokenMeta.getAttribute('content');
        }

        fetch(form.action, {
            method: 'POST',
            headers: headers,
            body: formData
        })
        .then(async response => {
            const data = await response.json().catch(() => ({}));
            if (!response.ok) {
                throw new Error(data.message || 'Không thể thêm sản phẩm vào giỏ hàng.');
            }
            return data;
        })
        .then(data => {
            // 1. Update Cart Badge Count
            const cartBadge = document.getElementById('cartBadge');
            if (cartBadge && typeof data.totalQuantity !== 'undefined') {
                cartBadge.textContent = data.totalQuantity;
                cartBadge.style.display = data.totalQuantity > 0 ? 'inline-block' : 'none';

                // Bounce animation
                cartBadge.classList.remove('cart-badge-bounce');
                void cartBadge.offsetWidth; // trigger reflow
                cartBadge.classList.add('cart-badge-bounce');
            }

            // 2. Pulse Cart Nav Button
            const cartNavBtn = document.getElementById('cartNavButton');
            if (cartNavBtn) {
                cartNavBtn.classList.remove('btn-cart-pulse');
                void cartNavBtn.offsetWidth;
                cartNavBtn.classList.add('btn-cart-pulse');
            }

            // 3. Show Toast Notification
            const toastEl = document.getElementById('cartToast');
            if (toastEl) {
                const toastMsg = document.getElementById('cartToastMessage');
                if (toastMsg) {
                    toastMsg.textContent = data.message || 'Đã thêm hoa quả vào giỏ hàng thành công!';
                }
                toastEl.className = 'toast align-items-center text-white bg-success border-0 shadow-lg';
                const toast = bootstrap.Toast.getOrCreateInstance(toastEl, { delay: 3500 });
                toast.show();
            }

            // 4. Temporary success button feedback
            if (submitBtn) {
                submitBtn.innerHTML = '<i class="bi bi-check2-circle me-1"></i> Đã Thêm!';
                submitBtn.classList.remove('btn-primary', 'btn-outline-success');
                submitBtn.classList.add('btn-success');

                setTimeout(() => {
                    submitBtn.innerHTML = originalBtnHtml;
                    submitBtn.disabled = false;
                    submitBtn.classList.remove('btn-success');
                    if (hadOutline) {
                        submitBtn.classList.add('btn-outline-success');
                    } else {
                        submitBtn.classList.add('btn-primary');
                    }
                }, 1200);
            }
        })
        .catch(err => {
            console.error('Lỗi thêm giỏ hàng:', err);

            // Show error Toast
            const toastEl = document.getElementById('cartToast');
            if (toastEl) {
                const toastMsg = document.getElementById('cartToastMessage');
                if (toastMsg) {
                    toastMsg.textContent = err.message || 'Lỗi khi thêm sản phẩm vào giỏ hàng';
                }
                toastEl.className = 'toast align-items-center text-white bg-danger border-0 shadow-lg';
                const toast = bootstrap.Toast.getOrCreateInstance(toastEl, { delay: 4000 });
                toast.show();
            }

            if (submitBtn) {
                submitBtn.innerHTML = originalBtnHtml;
                submitBtn.disabled = false;
            }
        });
    });
});
