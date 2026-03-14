document.addEventListener('DOMContentLoaded', function() {
    // Calculate initial summary
    calculateSummary();

    // Setup address change listener
    setupAddressListener();

    // Setup voucher listeners
    setupVoucherListeners();

    // Set item count
    document.getElementById('itemCount').textContent =
        document.querySelectorAll('.product-item').length;

    // Setup form validation
    setupFormValidation();
});

function calculateSummary() {
    // Calculate subtotal from product items
    let subtotal = 0;
    document.querySelectorAll('.product-item').forEach(item => {
        const priceText = item.querySelector('.col-md-2:nth-child(2)').textContent
            .replace('₫', '').trim();
        const qtyText = item.querySelector('.col-md-2:nth-child(3)').textContent.trim();
        const price = parseFloat(priceText.replace(/,/g, '')) || 0;
        const qty = parseInt(qtyText) || 0;

        subtotal += price * qty;
    });

    // Shipping fee (cố định)
    const shippingFee = 25000;

    // Calculate discount if voucher selected
    let discount = 0;
    const selectedVouchers = document.querySelectorAll('input[type="radio"][name^="shopVouchers"]:checked');
    selectedVouchers.forEach(voucherInput => {
        // Lấy thông tin voucher từ label
        const label = document.querySelector(`label[for="${voucherInput.id}"]`);
        if (label) {
            const discountText = label.querySelector('.voucher-discount').textContent;
            if (discountText.includes('%')) {
                const percent = parseFloat(discountText.match(/\d+/)[0]);
                discount += Math.floor(subtotal * percent / 100);
            } else if (discountText.includes('₫')) {
                const amount = parseFloat(discountText.replace(/[^\d]/g, '')) || 0;
                discount += amount;
            }
        }
    });

    // Update display
    const formattedSubtotal = new Intl.NumberFormat('vi-VN').format(Math.floor(subtotal));
    const formattedShipping = new Intl.NumberFormat('vi-VN').format(shippingFee);
    const formattedDiscount = new Intl.NumberFormat('vi-VN').format(Math.floor(discount));

    document.getElementById('subtotal').textContent = '₫' + formattedSubtotal;
    document.getElementById('shippingFee').textContent = '₫' + formattedShipping;
    document.getElementById('discount').textContent = '-₫' + formattedDiscount;

    const total = subtotal + shippingFee - discount;
    document.getElementById('total').textContent = '₫' + new Intl.NumberFormat('vi-VN').format(Math.floor(total));

    // Show/hide discount section
    if (discount > 0) {
        document.getElementById('discountSection').style.display = 'block';
    } else {
        document.getElementById('discountSection').style.display = 'none';
    }
}

function setupAddressListener() {
    const addressInputs = document.querySelectorAll('input[name="addressId"]');
    addressInputs.forEach(input => {
        input.addEventListener('change', function() {
            const label = document.querySelector(`label[for="${this.id}"]`);
            if (label) {
                const nameElement = label.querySelector('.address-info strong');
                const phoneElement = label.querySelector('.address-info .text-muted');
                const addressElement = label.querySelector('.address-info p');

                let displayText = '';
                if (nameElement) displayText += nameElement.textContent + ' ';
                if (phoneElement) displayText += phoneElement.textContent + ' ';
                if (addressElement) displayText += addressElement.textContent;

                document.getElementById('selectedAddress').textContent = displayText.trim() || 'Chưa chọn';
            }
        });
    });

    // Trigger change event for default address
    const defaultAddress = document.querySelector('input[name="addressId"]:checked');
    if (defaultAddress) {
        defaultAddress.dispatchEvent(new Event('change'));
    }
}

function setupVoucherListeners() {
    document.querySelectorAll('input[type="radio"][name^="shopVouchers"]').forEach(input => {
        input.addEventListener('change', function() {
            calculateSummary();
        });
    });
}

function setupFormValidation() {
    const form = document.getElementById('checkoutForm');
    form.addEventListener('submit', function(e) {
        // Validate address selection
        const addressId = document.querySelector('input[name="addressId"]:checked');
        if (!addressId) {
            e.preventDefault();
            alert('Vui lòng chọn địa chỉ giao hàng');
            return false;
        }

        // Validate payment method
        const paymentMethod = document.querySelector('input[name="paymentMethod"]');
        if (!paymentMethod.value.trim()) {
            e.preventDefault();
            alert('Vui lòng nhập phương thức thanh toán');
            return false;
        }

        // Form sẽ tự động gửi tất cả hidden inputs
        return true;
    });
}