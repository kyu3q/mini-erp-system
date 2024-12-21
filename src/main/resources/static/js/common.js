document.addEventListener('DOMContentLoaded', function() {
    // ツールチップの初期化
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.forEach(function(tooltipTriggerEl) {
        new bootstrap.Tooltip(tooltipTriggerEl, {
            trigger: 'hover'
        });
    });

    // Select2の共通設定
    if (typeof $.fn.select2 !== 'undefined') {
        $.fn.select2.defaults.set('theme', 'bootstrap-5');
        $.fn.select2.defaults.set('language', 'ja');
        $.fn.select2.defaults.set('width', '100%');
    }

    // DataTablesの共通設定
    if (typeof $.fn.dataTable !== 'undefined') {
        $.extend(true, $.fn.dataTable.defaults, {
            language: {
                url: "//cdn.datatables.net/plug-ins/1.13.7/i18n/ja.json"
            },
            processing: true,
            responsive: true,
            stateSave: true,
            pageLength: 50,
            lengthMenu: [[10, 25, 50, 100, -1], [10, 25, 50, 100, '全件']],
            dom: "<'row'<'col-sm-6'l><'col-sm-6'f>>" +
                 "<'row'<'col-sm-12'tr>>" +
                 "<'row'<'col-sm-5'i><'col-sm-7'p>>"
        });
    }
});