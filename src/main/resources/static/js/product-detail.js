document.addEventListener('DOMContentLoaded', function() {
    const attributeButtons = document.querySelectorAll('.attribute-value-btn');
    const quantityInput = document.querySelector('input[type="number"]');
    const addToCartBtn = document.querySelector('.btn-add-to-cart');
    const buyNowBtn = document.querySelector('.btn-buy-now');
    const priceSection = document.querySelector('.price-section');
    const quantitySection = document.querySelector('.quantity-section');

    let selectedAttributes = {};
    let allVariants = [];
    let totalAttributesCount = 0;

    // Load variants data from HTML
    document.querySelectorAll('.variant-data').forEach(variantEl => {
        const variant = {
            id: variantEl.dataset.variantId,
            price: parseFloat(variantEl.dataset.variantPrice),
            stock: parseInt(variantEl.dataset.variantStock),
            attributes: {}
        };

        // Parse attributes
        variantEl.querySelectorAll('.variant-attribute').forEach(attrEl => {
            const attrName = attrEl.dataset.attrName;
            const attrValue = attrEl.dataset.attrValue;
            variant.attributes[attrName] = attrValue;
        });

        allVariants.push(variant);
    });

    // Count total unique attributes
    const allAttributeNames = new Set();
    allVariants.forEach(variant => {
        Object.keys(variant.attributes).forEach(key => allAttributeNames.add(key));
    });
    totalAttributesCount = allAttributeNames.size;

    // Khởi tạo trạng thái ban đầu
    updateUI();

    // Xử lý khi click vào attribute button
    attributeButtons.forEach(button => {
        button.addEventListener('click', function() {
            const attributeName = this.dataset.attributeName;
            const attributeValue = this.dataset.attributeValue;

            // Remove active class from siblings
            const siblings = this.parentElement.querySelectorAll('.attribute-value-btn');
            siblings.forEach(btn => btn.classList.remove('active'));

            // Add active class to clicked button
            this.classList.add('active');

            // Update selected attributes
            selectedAttributes[attributeName] = attributeValue;

            // Update UI
            updateUI();
        });
    });

    function updateUI() {
        const matchedVariant = findMatchingVariant();

        if (matchedVariant) {
            // Hiển thị giá cụ thể
            priceSection.innerHTML = `
                <div class="price-single">
                    <span class="price-symbol">₫</span>
                    <span class="price-value">${formatPrice(matchedVariant.price)}</span>
                </div>
            `;

            // Hiển thị stock
            let stockInfo = document.getElementById('stock-info');
            if (!stockInfo) {
                stockInfo = document.createElement('div');
                stockInfo.id = 'stock-info';
                stockInfo.className = 'stock-info mb-3 text-muted';
                quantitySection.parentElement.insertBefore(stockInfo, quantitySection);
            }
            stockInfo.textContent = `Còn lại: ${matchedVariant.stock} sản phẩm`;
            stockInfo.style.display = 'block';

            // Update max quantity
            quantityInput.max = matchedVariant.stock;
            if (parseInt(quantityInput.value) > matchedVariant.stock) {
                quantityInput.value = matchedVariant.stock > 0 ? matchedVariant.stock : 0;
            }

            // Enable buttons nếu còn hàng
            if (matchedVariant.stock > 0) {
                addToCartBtn.disabled = false;
                buyNowBtn.disabled = false;
                addToCartBtn.classList.remove('disabled');
                buyNowBtn.classList.remove('disabled');
            } else {
                addToCartBtn.disabled = true;
                buyNowBtn.disabled = true;
                addToCartBtn.classList.add('disabled');
                buyNowBtn.classList.add('disabled');
            }

            // Store variant ID for cart operations
            addToCartBtn.dataset.variantId = matchedVariant.id;
            buyNowBtn.dataset.variantId = matchedVariant.id;

        } else {
            // Hiển thị range price
            const minPrice = document.querySelector('.original-price-range').dataset.minPrice;
            const maxPrice = document.querySelector('.original-price-range').dataset.maxPrice;

            priceSection.innerHTML = `
                <div class="price-range">
                    <span class="price-symbol">₫</span>
                    <span class="price-min">${formatPrice(parseFloat(minPrice))}</span>
                    <span class="mx-2">-</span>
                    <span class="price-max">${formatPrice(parseFloat(maxPrice))}</span>
                </div>
            `;

            // Hide stock
            const stockInfo = document.getElementById('stock-info');
            if (stockInfo) {
                stockInfo.style.display = 'none';
            }

            // Disable buttons
            addToCartBtn.disabled = true;
            buyNowBtn.disabled = true;
            addToCartBtn.classList.add('disabled');
            buyNowBtn.classList.add('disabled');
        }
    }

    function findMatchingVariant() {
        // Kiểm tra xem đã chọn đủ tất cả attributes chưa
        const selectedKeys = Object.keys(selectedAttributes);
        if (selectedKeys.length !== totalAttributesCount) {
            return null;
        }

        // Tìm variant khớp với tất cả attributes đã chọn
        return allVariants.find(variant => {
            return Object.keys(variant.attributes).every(attrName => {
                return selectedAttributes[attrName] === variant.attributes[attrName];
            });
        });
    }

    function formatPrice(price) {
        return new Intl.NumberFormat('vi-VN').format(price);
    }

    // Xử lý quantity controls
    const decreaseBtn = document.querySelector('.quantity-control .btn-decrease');
    const increaseBtn = document.querySelector('.quantity-control .btn-increase');

    if (decreaseBtn) {
        decreaseBtn.addEventListener('click', function() {
            const currentValue = parseInt(quantityInput.value);
            if (currentValue > 1) {
                quantityInput.value = currentValue - 1;
            }
        });
    }

    if (increaseBtn) {
        increaseBtn.addEventListener('click', function() {
            const currentValue = parseInt(quantityInput.value);
            const maxValue = parseInt(quantityInput.max) || Infinity;
            if (currentValue < maxValue) {
                quantityInput.value = currentValue + 1;
            }
        });
    }
});