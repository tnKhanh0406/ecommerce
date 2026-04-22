let attributeCount = 0;
let variantCount = 0;
const attributes = {};

document.addEventListener('DOMContentLoaded', function() {
    // Add Attribute Button
    document.getElementById('addAttributeBtn').onclick = function(e) {
        e.preventDefault();
        addAttribute();
    };

    // Add Variant Button
    document.getElementById('addVariantBtn').onclick = function(e) {
        e.preventDefault();
        addVariant();
    };

    document.getElementById('name').oninput = updateSummary;
    document.getElementById('productImages').onchange = handleProductImages;
    document.getElementById('createProductForm').onsubmit = handleFormSubmit;
});

function addAttribute() {
    const index = attributeCount++;
    const html = `
        <div class="attribute-card card mb-3" data-index="${index}">
            <div class="card-body">
                <div class="row align-items-end">
                    <div class="col-md-6">
                        <label class="font-weight-bold">Tên Thuộc Tính</label>
                        <input type="text" 
                               class="form-control attribute-name"
                               name="attr_name_${index}"
                               placeholder="VD: Màu sắc, Kích cỡ"
                               required>
                    </div>
                    <div class="col-md-6">
                        <button type="button" class="btn btn-danger btn-sm remove-attribute-btn" data-index="${index}">
                            🗑️ Xóa
                        </button>
                    </div>
                </div>

                <div class="mt-3">
                    <label class="font-weight-bold">Giá Trị Thuộc Tính (cách nhau bằng dấu phẩy)</label>
                    <textarea class="form-control attribute-values"
                              name="attr_values_${index}"
                              rows="3"
                              placeholder="VD: Đỏ, Xanh, Vàng"
                              required></textarea>
                </div>
            </div>
        </div>
    `;

    document.getElementById('attributesContainer').insertAdjacentHTML('beforeend', html);

    // Add event listener for remove button
    const removeBtn = document.querySelector(`.remove-attribute-btn[data-index="${index}"]`);
    removeBtn.addEventListener('click', function(e) {
        e.preventDefault();
        removeAttribute(index);
    });

    // Add change listeners
    const nameInput = document.querySelector(`input[name="attr_name_${index}"]`);
    const valuesInput = document.querySelector(`textarea[name="attr_values_${index}"]`);

    nameInput.onchange = valuesInput.onchange = function() {
        attributes[index] = {
            name: nameInput.value,
            values: valuesInput.value
        };
        generateVariantsFromAttributes();
    };

    updateSummary();
}

function removeAttribute(index) {
    document.querySelector(`[data-index="${index}"]`).remove();
    delete attributes[index];
    generateVariantsFromAttributes();
    updateSummary();
}

function addVariant() {
    // Check if there are attributes defined
    const hasAttributes = Object.values(attributes).some(attr => attr && attr.name && attr.values);
    
    if (!hasAttributes) {
        alert('Vui lòng thêm thuộc tính trước khi thêm phân loại.\nPhân loại sẽ được tự động sinh từ tổ hợp thuộc tính.');
        return;
    }
    
    alert('Phân loại được tự động sinh từ thuộc tính. Vui lòng chỉnh sửa thuộc tính để thêm hoặc xóa phân loại.');
}

function removeVariant(index) {
    document.querySelector(`[data-variant-index="${index}"]`).remove();
    updateSummary();
}

function updateVariantAttributeSelections() {
    document.querySelectorAll('.variant-card').forEach((variantCard, variantIndex) => {
        const attrSection = variantCard.querySelector(`#variantAttributesSection_${variantIndex}`);
        if (!attrSection) return;

        let html = '';
        Object.keys(attributes).forEach(attrIndex => {
            const attr = attributes[attrIndex];
            if (attr && attr.name && attr.values) {
                const values = attr.values.split(/[,\n]/).map(v => v.trim()).filter(v => v);

                html += `
                    <div class="form-group">
                        <label class="font-weight-bold">${attr.name}</label>
                        <select class="form-control" name="variant_attr_${variantIndex}_${attr.name}" required>
                            <option value="">-- Chọn ${attr.name} --</option>
                            ${values.map(v => `<option value="${v}">${v}</option>`).join('')}
                        </select>
                    </div>
                `;
            }
        });

        attrSection.innerHTML = html;
    });
}

function generateVariantsFromAttributes() {
    // Collect all valid attributes
    const validAttributes = Object.entries(attributes)
        .filter(([_, attr]) => attr && attr.name && attr.values)
        .map(([_, attr]) => ({
            name: attr.name,
            values: attr.values.split(/[,\n]/).map(v => v.trim()).filter(v => v)
        }));

    // If no attributes, do nothing
    if (validAttributes.length === 0) {
        return;
    }

    // Generate Cartesian product of all attribute values
    const combinations = cartesianProduct(validAttributes.map(a => a.values));

    // Clear existing variants
    document.getElementById('variantsContainer').innerHTML = '';
    variantCount = 0;

    // Create variants for each combination
    combinations.forEach((combination, combIndex) => {
        createVariantFromCombination(combination, validAttributes, combIndex);
    });

    updateSummary();
}

function cartesianProduct(arrays) {
    if (arrays.length === 0) return [[]];
    if (arrays.length === 1) return arrays[0].map(item => [item]);

    const [first, ...rest] = arrays;
    const restProduct = cartesianProduct(rest);
    
    const result = [];
    first.forEach(item => {
        restProduct.forEach(combination => {
            result.push([item, ...combination]);
        });
    });
    return result;
}

function createVariantFromCombination(combination, attributes, combIndex) {
    const index = variantCount++;
    const variantName = combination.join(' - ');
    
    // Build attribute fields for this combination
    let attributeFieldsHtml = '';
    attributes.forEach((attr, attrIdx) => {
        attributeFieldsHtml += `
            <div class="form-group">
                <label class="font-weight-bold">${attr.name}</label>
                <input type="text" class="form-control" value="${combination[attrIdx]}" disabled style="background-color: #f0f0f0;">
                <input type="hidden" name="variant_attr_${index}_${attr.name}" value="${combination[attrIdx]}">
            </div>
        `;
    });

    const html = `
        <div class="variant-card card mb-4" data-variant-index="${index}">
            <div class="card-header bg-light d-flex justify-content-between align-items-center">
                <h6 class="mb-0">Phân Loại #${variantCount}: <strong>${variantName}</strong></h6>
                <button type="button" class="btn btn-danger btn-sm remove-variant-btn" data-index="${index}">
                    🗑️ Xóa
                </button>
            </div>
            <div class="card-body">
                <div class="mb-3 p-3 bg-light rounded">
                    ${attributeFieldsHtml}
                </div>

                <div class="form-group">
                    <label class="font-weight-bold">Ảnh Phân Loại (Tùy Chọn)</label>
                    <div class="custom-file mb-3">
                        <input type="file" 
                               class="custom-file-input variant-images"
                               name="variantImages_${index}"
                               accept="image/*"
                               multiple
                               onchange="handleVariantImages(event, ${index})">
                        <label class="custom-file-label">Chọn ảnh...</label>
                    </div>
                    <div class="variant-images-preview row mb-3"></div>
                </div>

                <div class="row">
                    <div class="col-md-4">
                        <div class="form-group">
                            <label class="font-weight-bold">SKU (Tùy Chọn)</label>
                            <input type="text" 
                                   class="form-control variant-sku"
                                   name="variant_sku_${index}"
                                   placeholder="VD: SKU001">
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="form-group">
                            <label class="font-weight-bold">Giá <span class="text-danger">*</span></label>
                            <input type="number" 
                                   class="form-control variant-price"
                                   name="variant_price_${index}"
                                   placeholder="0"
                                   min="1"
                                   step="1"
                                   required>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="form-group">
                            <label class="font-weight-bold">Kho <span class="text-danger">*</span></label>
                            <input type="number" 
                                   class="form-control variant-stock"
                                   name="variant_stock_${index}"
                                   placeholder="0"
                                   min="0"
                                   required>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    document.getElementById('variantsContainer').insertAdjacentHTML('beforeend', html);

    // Add event listener for remove button
    const removeBtn = document.querySelector(`.remove-variant-btn[data-index="${index}"]`);
    removeBtn.addEventListener('click', function(e) {
        e.preventDefault();
        removeVariant(index);
    });
}

function handleProductImages(e) {
    const files = Array.from(e.target.files);
    const preview = document.getElementById('productImagesPreview');
    preview.innerHTML = '';

    files.forEach((file) => {
        const reader = new FileReader();
        reader.onload = function(event) {
            preview.insertAdjacentHTML('beforeend', `
                <div class="col-md-3 mb-3">
                    <div class="card">
                        <img src="${event.target.result}" class="card-img-top" style="height: 150px; object-fit: cover;">
                        <div class="card-body p-2">
                            <small class="text-truncate">${file.name}</small>
                        </div>
                    </div>
                </div>
            `);
        };
        reader.readAsDataURL(file);
    });

    updateSummary();
}

function handleVariantImages(e, variantIndex) {
    const files = Array.from(e.target.files);
    const variantCard = document.querySelector(`[data-variant-index="${variantIndex}"]`);
    const preview = variantCard.querySelector('.variant-images-preview');
    preview.innerHTML = '';

    files.forEach((file) => {
        const reader = new FileReader();
        reader.onload = function(event) {
            preview.insertAdjacentHTML('beforeend', `
                <div class="col-md-3 mb-2">
                    <img src="${event.target.result}" class="img-fluid img-thumbnail" style="height: 100px; object-fit: cover;">
                </div>
            `);
        };
        reader.readAsDataURL(file);
    });
}

function updateSummary() {
    document.getElementById('summaryName').textContent = document.getElementById('name').value || 'Chưa nhập';
    document.getElementById('summaryImages').textContent = document.getElementById('productImages').files.length + ' ảnh';
    document.getElementById('summaryAttributes').textContent = Object.keys(attributes).filter(k => attributes[k] && attributes[k].name).length + ' thuộc tính';
    document.getElementById('summaryVariants').textContent = document.querySelectorAll('.variant-card').length + ' phân loại';
}

function handleFormSubmit(e) {
    e.preventDefault();

    const name = document.getElementById('name').value;
    const description = document.getElementById('description').value;
    const variantCount = document.querySelectorAll('.variant-card').length;

    if (!name.trim() || name.length < 10) {
        alert('Tên sản phẩm phải tối thiểu 10 ký tự');
        return;
    }

    if (!description.trim() || description.length < 20) {
        alert('Mô tả sản phẩm phải tối thiểu 20 ký tự');
        return;
    }

    if (variantCount === 0) {
        alert('Bạn phải thêm ít nhất 1 phân loại');
        return;
    }

    document.querySelectorAll('.variant-card').forEach((variant, i) => {
        const price = variant.querySelector('.variant-price').value;
        const stock = variant.querySelector('.variant-stock').value;

        if (!price || price < 1) {
            alert(`Phân loại #${i+1}: Giá phải >= 1`);
            return;
        }

        if (stock === '' || stock < 0) {
            alert(`Phân loại #${i+1}: Vui lòng nhập số lượng kho hợp lệ`);
            return;
        }
    });

    e.target.submit();
}