let manageVariantCount = 0;
let manageAttributeCount = 0;

function updateFileLabel(input) {
    const label = input.closest('.custom-file').querySelector('.custom-file-label');
    if (!label) return;

    if (!input.files || input.files.length === 0) {
        label.textContent = 'Chọn file...';
        return;
    }

    label.textContent = input.files.length === 1
        ? input.files[0].name
        : `${input.files.length} file đã chọn`;
}

function previewFiles(input, previewSelector) {
    const preview = document.querySelector(previewSelector);
    if (!preview) return;

    preview.innerHTML = '';
    Array.from(input.files || []).forEach((file) => {
        const reader = new FileReader();
        reader.onload = function(event) {
            preview.insertAdjacentHTML('beforeend', `
                <div class="col-4 col-md-3 mb-2">
                    <img src="${event.target.result}" class="img-fluid" style="height: 90px; width: 100%; object-fit: cover;">
                </div>
            `);
        };
        reader.readAsDataURL(file);
    });
}

function handleBasicImagesChange(input) {
    updateFileLabel(input);
    previewFiles(input, '#basicImagesPreview');
}

function handleVariantImagesChange(input, variantIndex, previewSelector) {
    updateFileLabel(input);
    previewFiles(input, previewSelector);
}

function removeExistingImage(button) {
    const imageItem = button.closest('.existing-image-item');
    if (imageItem) {
        imageItem.remove();
    }
}

function removeManageVariant(index) {
    const row = document.querySelector(`[data-manage-variant-index="${index}"]`);
    if (row) {
        row.remove();
        reindexManageVariantRows();
        renderAllManageVariantAttributes();
    }
}

function addManageVariant() {
    const template = document.getElementById('manageVariantTemplate');
    const container = document.getElementById('manageVariantsContainer');
    if (!template || !container) return;

    const nextIndex = manageVariantCount++;
    const wrapper = document.createElement('div');
    wrapper.innerHTML = template.innerHTML.replaceAll('[INDEX]', nextIndex);
    const node = wrapper.firstElementChild;
    container.appendChild(node);

    rebindVariantImageInputs();
    reindexManageVariantRows();
    renderAllManageVariantAttributes();
}

function removeManageAttribute(index) {
    const row = document.querySelector(`[data-attr-index="${index}"]`);
    if (row) {
        row.remove();
        reindexManageAttributeRows();
        renderAllManageVariantAttributes();
    }
}

function addManageAttribute() {
    const template = document.getElementById('manageAttributeTemplate');
    const container = document.getElementById('manageAttributesContainer');
    if (!template || !container) return;

    const nextIndex = manageAttributeCount++;
    const wrapper = document.createElement('div');
    wrapper.innerHTML = template.innerHTML.replaceAll('[INDEX]', nextIndex);
    const node = wrapper.firstElementChild;
    container.appendChild(node);

    reindexManageAttributeRows();
    bindManageAttributeInputs();
    renderAllManageVariantAttributes();
}

function reindexManageVariantRows() {
    const rows = document.querySelectorAll('.manage-variant-row');
    rows.forEach((row, index) => {
        row.dataset.manageVariantIndex = index;

        const title = row.querySelector('h6');
        if (title && !title.textContent.includes('moi')) {
            title.textContent = `Variant #${index + 1}`;
        }

        const removeBtn = row.querySelector('button.btn-outline-danger');
        if (removeBtn) {
            removeBtn.setAttribute('onclick', `removeManageVariant(${index})`);
        }

        const skuInput = row.querySelector('input[name^="variant_sku_"]');
        if (skuInput) skuInput.name = `variant_sku_${index}`;

        const priceInput = row.querySelector('input[name^="variant_price_"]');
        if (priceInput) priceInput.name = `variant_price_${index}`;

        const stockInput = row.querySelector('input[name^="variant_stock_"]');
        if (stockInput) stockInput.name = `variant_stock_${index}`;

        const attrSection = row.querySelector('.manage-variant-attributes');
        if (attrSection) attrSection.id = `manageVariantAttributes_${index}`;

        const fileInput = row.querySelector('input[type="file"][name^="variantImages_"]');
        if (fileInput) {
            fileInput.name = `variantImages_${index}`;
            fileInput.dataset.variantIndex = String(index);
        }

        const preview = row.querySelector('.manage-variant-preview');
        if (preview) preview.id = `variantPreview_${index}`;

        const existingPreview = row.querySelector('[id^="variantExistingPreview_"]');
        if (existingPreview) {
            existingPreview.id = `variantExistingPreview_${index}`;
        }

        const existingInputs = row.querySelectorAll('input[name^="variant_existing_image_"]');
        existingInputs.forEach((input, imageIndex) => {
            input.name = `variant_existing_image_${index}_${imageIndex}`;
        });
    });

    manageVariantCount = rows.length;
}

function reindexManageAttributeRows() {
    const rows = document.querySelectorAll('.manage-attribute-row');
    rows.forEach((row, index) => {
        row.dataset.attrIndex = index;

        const removeBtn = row.querySelector('button.btn-outline-danger');
        if (removeBtn) {
            removeBtn.setAttribute('onclick', `removeManageAttribute(${index})`);
        }

        const nameInput = row.querySelector('.manage-attr-name');
        if (nameInput) nameInput.name = `attr_name_${index}`;

        const valuesInput = row.querySelector('.manage-attr-values');
        if (valuesInput) valuesInput.name = `attr_values_${index}`;
    });

    manageAttributeCount = rows.length;
}

function parseAttributeValues(raw) {
    return String(raw || '')
        .split(/[\n,]/)
        .map((item) => item.trim())
        .filter((item) => item.length > 0);
}

function getManageAttributes() {
    const attrs = [];
    document.querySelectorAll('.manage-attribute-row').forEach((row) => {
        const name = row.querySelector('.manage-attr-name')?.value?.trim();
        const valuesRaw = row.querySelector('.manage-attr-values')?.value;
        const values = parseAttributeValues(valuesRaw);

        if (name && values.length > 0) {
            attrs.push({ name, values });
        }
    });
    return attrs;
}

function renderAllManageVariantAttributes() {
    const attrs = getManageAttributes();

    document.querySelectorAll('.manage-variant-row').forEach((row) => {
        const variantIndex = Number(row.dataset.manageVariantIndex);
        const section = row.querySelector('.manage-variant-attributes');
        if (!section) return;

        const selectedMap = {};
        row.querySelectorAll('.existing-variant-attr').forEach((input) => {
            selectedMap[input.dataset.attribute] = input.dataset.value;
        });

        row.querySelectorAll('select[name^="variant_attr_"]').forEach((select) => {
            const marker = `variant_attr_${variantIndex}_`;
            if (select.name.startsWith(marker)) {
                const attrName = select.name.substring(marker.length);
                selectedMap[attrName] = select.value;
            }
        });

        if (attrs.length === 0) {
            section.innerHTML = '<div class="text-muted small">Hãy thêm ít nhất 1 thuộc tính để cấu hình variant.</div>';
            return;
        }

        let html = '';
        attrs.forEach((attr) => {
            const options = attr.values
                .map((value) => `<option value="${value}" ${selectedMap[attr.name] === value ? 'selected' : ''}>${value}</option>`)
                .join('');

            html += `
                <div class="form-group mb-2">
                    <label class="font-weight-bold">${attr.name}</label>
                    <select class="form-control" name="variant_attr_${variantIndex}_${attr.name}" required>
                        <option value="">-- Chọn ${attr.name} --</option>
                        ${options}
                    </select>
                </div>
            `;
        });

        section.innerHTML = html;
    });
}

function bindManageAttributeInputs() {
    document.querySelectorAll('.manage-attr-name, .manage-attr-values').forEach((input) => {
        input.removeEventListener('input', renderAllManageVariantAttributes);
        input.addEventListener('input', renderAllManageVariantAttributes);
    });
}

function rebindVariantImageInputs() {
    document.querySelectorAll('.js-variant-images').forEach((input) => {
        if (input.dataset.bound === 'true') {
            return;
        }

        input.addEventListener('change', function() {
            const variantIndex = this.dataset.variantIndex;
            handleVariantImagesChange(this, variantIndex, `#variantPreview_${variantIndex}`);
        });
        input.dataset.bound = 'true';
    });
}

function validateManageVariantsForm(event) {
    const rows = document.querySelectorAll('.manage-variant-row');
    const attrs = getManageAttributes();

    if (rows.length === 0) {
        event.preventDefault();
        alert('Bạn phải giữ ít nhất 1 variant hoặc thêm variant mới');
        return false;
    }

    if (attrs.length === 0) {
        event.preventDefault();
        alert('Bạn phải có ít nhất 1 thuộc tính với giá trị hợp lệ');
        return false;
    }

    reindexManageAttributeRows();
    reindexManageVariantRows();
    renderAllManageVariantAttributes();

    return true;
}

document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.js-file-label').forEach((input) => {
        input.addEventListener('change', function() {
            updateFileLabel(this);
        });
    });

    document.querySelectorAll('.js-basic-images').forEach((input) => {
        input.addEventListener('change', function() {
            handleBasicImagesChange(this);
        });
    });

    rebindVariantImageInputs();

    const manageContainer = document.getElementById('manageVariantsContainer');
    if (manageContainer) {
        manageVariantCount = manageContainer.querySelectorAll('.manage-variant-row').length;
    }

    const manageAttrContainer = document.getElementById('manageAttributesContainer');
    if (manageAttrContainer) {
        manageAttributeCount = manageAttrContainer.querySelectorAll('.manage-attribute-row').length;
    }

    bindManageAttributeInputs();
    reindexManageAttributeRows();
    reindexManageVariantRows();
    renderAllManageVariantAttributes();

    const addManageBtn = document.getElementById('addManageVariantBtn');
    if (addManageBtn) {
        addManageBtn.addEventListener('click', function() {
            addManageVariant();
        });
    }

    const addManageAttributeBtn = document.getElementById('addManageAttributeBtn');
    if (addManageAttributeBtn) {
        addManageAttributeBtn.addEventListener('click', function() {
            addManageAttribute();
        });
    }

    const manageForm = document.getElementById('manageVariantsForm');
    if (manageForm) {
        manageForm.addEventListener('submit', validateManageVariantsForm);
    }
});
