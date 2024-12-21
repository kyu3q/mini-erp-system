$(document).ready(function() {
    // 日本語ロケールの設定
    moment.locale('ja');

    // DataTablesの初期化
    const table = $('#orderTable').DataTable({
        ajax: {
            url: '/orders/search',
            dataSrc: '',
            data: function(d) {
                return {
                    orderNumber: $('#orderNumber').val(),
                    orderDateFrom: $('#orderDateFrom').val(),
                    orderDateTo: $('#orderDateTo').val(),
                    customerId: $('#customerId').val(),
                    itemId: $('#itemId').val(),
                    status: $('#status').val()
                };
            }
        },
        columns: [
            {
                data: null,
                render: function(data, type, row) {
                    return `<a href="/orders/${row.id}/edit" class="text-primary text-decoration-none">
                        <i class="fas fa-file-alt me-1"></i>${row.orderNumber}</a>`;
                }
            },
            {
                data: 'orderDate',
                render: function(data) {
                    return data ? moment(data).format('YYYY/MM/DD') : '';
                }
            },
            { data: 'customerName' },
            {
                data: 'deliveryDate',
                render: function(data) {
                    return data ? moment(data).format('YYYY/MM/DD') : '';
                }
            },
            {
                data: null,
                render: function(data, type, row) {
                    const statusClass = row.status === 'DRAFT' ? 'bg-secondary' :
                                      row.status === 'CONFIRMED' ? 'bg-primary' :
                                      row.status === 'PROCESSING' ? 'bg-info' :
                                      row.status === 'SHIPPED' ? 'bg-success' :
                                      row.status === 'DELIVERED' ? 'bg-success' :
                                      row.status === 'CANCELLED' ? 'bg-danger' : 'bg-secondary';
                    return `<span class="badge rounded-pill ${statusClass}">${row.statusDisplayName}</span>`;
                }
            },
            {
                data: 'totalAmount',
                className: 'text-end',
                render: function(data) {
                    return `<span class="fw-bold">${new Intl.NumberFormat('ja-JP').format(data)}</span>
                        <small class="text-muted">円</small>`;
                }
            },
            {
                data: null,
                className: 'text-center',
                render: function(data, type, row) {
                    return `<div class="btn-group">
                        <a href="/orders/${row.id}/edit" class="btn btn-sm btn-outline-primary"
                           data-bs-toggle="tooltip" data-bs-title="編集">
                            <i class="fas fa-edit"></i>
                        </a>
                        <button type="button" class="btn btn-sm btn-outline-danger"
                                onclick="confirmDelete(${row.id})"
                                data-bs-toggle="tooltip" data-bs-title="削除">
                            <i class="fas fa-trash-alt"></i>
                        </button>
                    </div>`;
                }
            }
        ],
        order: [[1, 'desc']], // 受注日の降順でソート
        columnDefs: [
            { orderable: false, targets: [6] }, // 操作列はソート不可
            { searchable: false, targets: [6] }, // 操作列は検索対象外
            { responsivePriority: 1, targets: [0, 1, 2] }, // 優先して表示する列
            { responsivePriority: 2, targets: [4, 5] } // 次に優先して表示する列
        ],
        drawCallback: function(settings) {
            // ツールチップの再初期化
            const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
            tooltipTriggerList.forEach(function(tooltipTriggerEl) {
                new bootstrap.Tooltip(tooltipTriggerEl, {
                    trigger: 'hover'
                });
            });
        }
    });

    // 検索フォームのサブミット処理
    $('.search-form').on('submit', function(e) {
        e.preventDefault();
        table.ajax.reload();
    });

    // フォームクリア処理
    window.clearForm = function(form) {
        form.reset();
        table.ajax.reload();
    };

    // 削除確認処理
    window.confirmDelete = function(id) {
        const modal = $('#deleteModal');
        const form = modal.find('#deleteForm');
        form.attr('action', `/orders/${id}`);
        form.find('input[name="_method"]').val('DELETE');
        modal.modal('show');
    };

    // ファイル選択時の自動アップロード
    $('#file').on('change', function() {
        if (this.files.length > 0) {
            $('#importForm').submit();
        }
    });
});