document.addEventListener('DOMContentLoaded', function() {
    const selectAllCheckbox = document.getElementById('select-all');
    const selectAllBottomCheckbox = document.getElementById('select-all-bottom');
    const shopCheckboxes = document.querySelectorAll('.shop-checkbox');
    const itemCheckboxes = document.querySelectorAll('.item-checkbox');
    const checkoutBtn = document.getElementById('checkout-btn');
    const selectedCountSpan = document.getElementById('selected-count');
    const totalAmountSpan = document.getElementById('total-amount');

    // Sync two "select all" checkboxes
    if (selectAllCheckbox && selectAllBottomCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            selectAllBottomCheckbox.checked = this.checked;
            toggleAllItems(this.checked);
        });

        selectAllBottomCheckbox.addEventListener('change', function() {
            selectAllCheckbox.checked = this.checked;
            toggleAllItems(this.checked);
        });
    }

    // Shop checkbox change
    shopCheckboxes.forEach(shopCheckbox => {
        shopCheckbox.addEventListener('change', function() {
            const shopId = this.id.replace('shop-', '');
            const shopItems = document.querySelectorAll(`[data-shop-id="${shopId}"]`);
            shopItems.forEach(item => {
                item.checked = this.checked;
            });
            updateSummary();
            updateSelectAllStatus();
        });
    });

    // Item checkbox change
    itemCheckboxes.forEach(itemCheckbox => {
        itemCheckbox.addEventListener('change', function() {
            updateShopCheckboxStatus(this.dataset.shopId);
            updateSummary();
            updateSelectAllStatus();
        });
    });

    // Quantity controls
    document.querySelectorAll('.btn-qty-decrease').forEach(btn => {
        btn.addEventListener('click', function() {
            const input = this.parentElement.querySelector('.qty-input');
            const currentValue = parseInt(input.value);
            if (currentValue > 1) {
                input.value = currentValue - 1;
                input.form.submit();
            }
        });
    });

    document.querySelectorAll('.btn-qty-increase').forEach(btn => {
        btn.addEventListener('click', function() {
            const input = this.parentElement.querySelector('.qty-input');
            const currentValue = parseInt(input.value);
            const maxValue = parseInt(input.max);
            if (currentValue < maxValue) {
                input.value = currentValue + 1;
                input.form.submit();
            }
        });
    });

    function toggleAllItems(checked) {
        shopCheckboxes.forEach(cb => cb.checked = checked);
        itemCheckboxes.forEach(cb => cb.checked = checked);
        updateSummary();
    }

    function updateShopCheckboxStatus(shopId) {
        const shopItems = document.querySelectorAll(`[data-shop-id="${shopId}"]`);
        const shopCheckbox = document.getElementById(`shop-${shopId}`);

        if (shopCheckbox) {
            const allChecked = Array.from(shopItems).every(item => item.checked);
            shopCheckbox.checked = allChecked;
        }
    }

    function updateSelectAllStatus() {
        const allChecked = Array.from(itemCheckboxes).every(item => item.checked);
        if (selectAllCheckbox) selectAllCheckbox.checked = allChecked;
        if (selectAllBottomCheckbox) selectAllBottomCheckbox.checked = allChecked;
    }

    function updateSummary() {
        let total = 0;
        let count = 0;

        itemCheckboxes.forEach(checkbox => {
            if (checkbox.checked) {
                const cartItem = checkbox.closest('.cart-item');
                const qtyInput = cartItem.querySelector('.qty-input');
                const price = parseFloat(qtyInput.dataset.price);
                const quantity = parseInt(qtyInput.value);

                total += price * quantity;
                count++;
            }
        });

        if (selectedCountSpan) {
            selectedCountSpan.textContent = count;
        }

        if (totalAmountSpan) {
            totalAmountSpan.textContent = new Intl.NumberFormat('vi-VN').format(total);
        }

        if (checkoutBtn) {
            checkoutBtn.disabled = count === 0;
        }
    }

    // Initialize summary
    updateSummary();
});