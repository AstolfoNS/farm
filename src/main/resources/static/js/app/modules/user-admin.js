(function (window, $) {
    var FarmUserAdminModule = {};
    var state = {
        active: false,
        initialized: false,
        editIndex: -1,
        uploadRowIndex: -1
    };

    function motion() {
        if ($.isFunction(window.farmMotion)) {
            return window.farmMotion();
        }
        return {moduleEnterMs: 260, actionFeedbackMs: 1200};
    }

    function asNumber(value, def) {
        var n = Number(value);
        return isNaN(n) ? (def || 0) : n;
    }

    function escapeHtml(value) {
        return $("<div/>").text(value == null ? "" : String(value)).html();
    }

    function resolveHead(value) {
        var raw = value == null ? "" : String(value).trim();
        if (raw.length > 0) {
            return raw;
        }
        return farmResolveImg("ui/user/default-avatar.png");
    }

    function statCell(icon, value) {
        return "<span class='user-admin-stat-cell'>" +
            "<img src='" + escapeHtml(icon) + "' alt=''>" +
            escapeHtml(asNumber(value, 0)) +
            "</span>";
    }

    function renderAvatar(value) {
        return "<img class='user-admin-avatar' src='" + escapeHtml(resolveHead(value)) + "' alt='avatar'>";
    }

    function renderOps(index) {
        return "<a href='javascript:void(0)' class='user-admin-op upload' data-action='upload' data-index='" + index + "'>上传头像</a>" +
            "<a href='javascript:void(0)' class='user-admin-op save' data-action='save' data-index='" + index + "'>保存数据</a>";
    }

    function pagePayload(param) {
        var queryName = $("#userAdminName").textbox("getValue");
        return {
            name: queryName,
            page: asNumber(param.page, 1),
            rows: asNumber(param.rows, 5),
            sort: param.sort || "id",
            order: param.order || "asc"
        };
    }

    function beginRowEdit(index) {
        if (index === state.editIndex) {
            return true;
        }
        if (!endCurrentEdit()) {
            $("#userAdminGrid").datagrid("selectRow", state.editIndex);
            return false;
        }
        $("#userAdminGrid").datagrid("beginEdit", index);
        state.editIndex = index;
        return true;
    }

    function endCurrentEdit() {
        if (state.editIndex < 0) {
            return true;
        }
        if (!$("#userAdminGrid").datagrid("validateRow", state.editIndex)) {
            return false;
        }
        $("#userAdminGrid").datagrid("endEdit", state.editIndex);
        state.editIndex = -1;
        return true;
    }

    function cancelEdit() {
        if (state.editIndex < 0) {
            return;
        }
        var row = $("#userAdminGrid").datagrid("getRows")[state.editIndex];
        $("#userAdminGrid").datagrid("cancelEdit", state.editIndex);
        if (row && row._isNew) {
            $("#userAdminGrid").datagrid("deleteRow", state.editIndex);
        }
        state.editIndex = -1;
    }

    function showMessage(res) {
        var ok = FarmApi.isOk(res);
        var text = (res && res.msg) ? res.msg : (ok ? "操作成功" : "操作失败");
        if (ok) {
            $.messager.show({
                title: "消息",
                msg: text,
                timeout: motion().actionFeedbackMs,
                showType: "slide"
            });
            return;
        }
        $.messager.alert("提示", text);
    }

    function reload() {
        if (!state.initialized) {
            return;
        }
        state.editIndex = -1;
        $("#userAdminGrid").datagrid("reload");
    }

    function addUserRow() {
        if (!endCurrentEdit()) {
            return;
        }
        var rows = $("#userAdminGrid").datagrid("getRows");
        var index = rows.length;
        $("#userAdminGrid").datagrid("appendRow", {
            id: 0,
            username: "",
            nickname: "",
            experience: 0,
            score: 0,
            coin: 0,
            avatarPath: "",
            head: farmResolveImg("ui/user/default-avatar.png"),
            _isNew: true
        });
        $("#userAdminGrid").datagrid("selectRow", index);
        beginRowEdit(index);
    }

    function deleteSelectedUser() {
        var row = $("#userAdminGrid").datagrid("getSelected");
        if (!row) {
            $.messager.alert("提示", "请先选择一条记录");
            return;
        }
        var rowIndex = $("#userAdminGrid").datagrid("getRowIndex", row);
        if (!(row.id && asNumber(row.id, 0) > 0)) {
            if (rowIndex >= 0) {
                $("#userAdminGrid").datagrid("deleteRow", rowIndex);
                state.editIndex = -1;
            }
            return;
        }
        $.messager.confirm("确认", "确认删除当前用户吗？", function (ok) {
            if (!ok) {
                return;
            }
            FarmApi.userAdminDelete({id: row.id}, function (res) {
                showMessage(res);
                if (FarmApi.isOk(res)) {
                    reload();
                }
            }, function () {
                $.messager.alert("错误", "删除失败，请稍后重试");
            });
        });
    }

    function saveRow(index) {
        if (!beginRowEdit(index)) {
            return;
        }
        if (!endCurrentEdit()) {
            $.messager.alert("提示", "请先完善当前行数据");
            return;
        }
        var row = $("#userAdminGrid").datagrid("getRows")[index];
        if (!row) {
            $.messager.alert("提示", "未获取到当前行数据");
            return;
        }
        var payload = {
            id: row.id ? String(row.id) : "",
            username: $.trim(row.username || ""),
            nickname: $.trim(row.nickname || ""),
            experience: String(asNumber(row.experience, 0)),
            score: String(asNumber(row.score, 0)),
            coin: String(asNumber(row.coin, 0)),
            avatarPath: row.avatarPath || ""
        };
        FarmApi.userAdminSave(payload, function (res) {
            showMessage(res);
            if (!FarmApi.isOk(res)) {
                return;
            }
            reload();
            if (window.FarmHomeBridge && $.isFunction(window.FarmHomeBridge.refreshCurUser)) {
                window.FarmHomeBridge.refreshCurUser();
            }
            FarmApi.loginOptions(function (optRes) {
                if (FarmApi.isOk(optRes) && $.isArray(optRes.data)) {
                    $("#homeUserSelect").combobox("loadData", optRes.data);
                }
            });
        }, function () {
            $.messager.alert("错误", "保存失败，请稍后重试");
        });
    }

    function openUploadDialog(index) {
        $("#userAdminGrid").datagrid("selectRow", index);
        beginRowEdit(index);
        state.uploadRowIndex = index;
        $("#userAvatarFile").val("");
        $("#userAvatarUploadDialog").dialog("open");
    }

    function updateAvatarToRow(index, avatarPath, accessUrl) {
        if (index < 0) {
            return;
        }
        $("#userAdminGrid").datagrid("beginEdit", index);
        var editor = $("#userAdminGrid").datagrid("getEditor", {index: index, field: "avatarPath"});
        if (editor && editor.target) {
            editor.target.val(avatarPath);
        }
        var rows = $("#userAdminGrid").datagrid("getRows");
        if (!rows[index]) {
            return;
        }
        rows[index].avatarPath = avatarPath;
        rows[index].head = accessUrl || resolveHead(rows[index].head);
        $("#userAdminGrid").datagrid("refreshRow", index);
    }

    function uploadAvatar() {
        if (state.uploadRowIndex < 0) {
            $.messager.show({title: "提示", msg: "请先选择要上传头像的用户", timeout: motion().actionFeedbackMs, showType: "slide"});
            return;
        }
        if (!$("#userAvatarFile").val()) {
            $.messager.show({title: "提示", msg: "请选择需要上传的头像文件", timeout: motion().actionFeedbackMs, showType: "slide"});
            return;
        }
        var formData = new FormData($("#userAvatarUploadForm")[0]);
        $.ajax({
            url: "/file/saveHeadImg",
            type: "post",
            data: formData,
            processData: false,
            contentType: false,
            dataType: "json",
            success: function (res) {
                showMessage(res);
                if (!FarmApi.isOk(res) || !res.data) {
                    return;
                }
                var avatarPath = res.data.relativePath || res.data.path || "";
                var accessUrl = res.data.accessUrl || "";
                updateAvatarToRow(state.uploadRowIndex, avatarPath, accessUrl);
                $("#userAvatarUploadDialog").dialog("close");
            },
            error: function () {
                $.messager.show({title: "错误", msg: "上传失败，请稍后重试", timeout: motion().actionFeedbackMs, showType: "slide"});
            },
            complete: function () {
                $("#userAvatarFile").val("");
            }
        });
    }

    function bindEvents() {
        $("#userAvatarUploadDialog").dialog({
            cls: "farm-upload-window"
        });

        $("#userAdminSearchBtn").on("click", function () {
            endCurrentEdit();
            $("#userAdminGrid").datagrid("load", {page: 1});
        });
        $("#userAdminName").textbox("textbox").on("keydown", function (event) {
            if (event.keyCode === 13) {
                $("#userAdminSearchBtn").trigger("click");
            }
        });
        $("#userAdminAddBtn").on("click", addUserRow);
        $("#userAdminCancelBtn").on("click", cancelEdit);
        $("#userAdminDeleteBtn").on("click", deleteSelectedUser);
        $("#userAdminCloseBtn").on("click", function () {
            if (window.FarmHomeBridge && $.isFunction(window.FarmHomeBridge.switchModule)) {
                window.FarmHomeBridge.switchModule("home");
            }
        });
        $("#userManagePanel").on("click", ".user-admin-op", function () {
            var action = String($(this).data("action") || "");
            var index = asNumber($(this).data("index"), -1);
            if (index < 0) {
                return;
            }
            if (action === "upload") {
                openUploadDialog(index);
                return;
            }
            if (action === "save") {
                saveRow(index);
            }
        });
        $("#userAvatarUploadBtn").on("click", uploadAvatar);
        $("#userAvatarCloseBtn").on("click", function () {
            $("#userAvatarUploadDialog").dialog("close");
        });
    }

    function initGrid() {
        if (state.initialized) {
            return;
        }
        $("#userAdminGrid").datagrid({
            fit: false,
            width: "100%",
            height: 422,
            fitColumns: true,
            striped: true,
            rownumbers: true,
            singleSelect: true,
            pagination: true,
            pageSize: 5,
            pageList: [5, 10, 20],
            idField: "id",
            loader: function (param, success, error) {
                FarmApi.userAdminPage(pagePayload(param), function (res) {
                    if (!FarmApi.isOk(res) || !res.data) {
                        success({total: 0, rows: []});
                        return;
                    }
                    var pageRows = [];
                    if ($.isArray(res.data.records)) {
                        pageRows = res.data.records;
                    } else if ($.isArray(res.data.rows)) {
                        pageRows = res.data.rows;
                    }
                    success({
                        total: asNumber(res.data.total, 0),
                        rows: pageRows
                    });
                }, function () {
                    error.apply(this, arguments);
                });
            },
            columns: [[
                {field: "id", title: "ID", width: 38, sortable: true},
                {
                    field: "head",
                    title: "头像",
                    width: 66,
                    formatter: function (value) {
                        return renderAvatar(value);
                    }
                },
                {
                    field: "username",
                    title: "用户名",
                    width: 76,
                    sortable: true,
                    editor: {type: "textbox", options: {required: true}}
                },
                {
                    field: "nickname",
                    title: "昵称",
                    width: 68,
                    sortable: true,
                    editor: {type: "textbox", options: {required: true}}
                },
                {
                    field: "experience",
                    title: "经验值",
                    width: 92,
                    sortable: true,
                    editor: {type: "numberbox", options: {min: 0, precision: 0}},
                    formatter: function (value) {
                        return statCell(farmResolveImg("ui/user/stat-exp.png"), value);
                    }
                },
                {
                    field: "score",
                    title: "积分",
                    width: 76,
                    sortable: true,
                    editor: {type: "numberbox", options: {min: 0, precision: 0}},
                    formatter: function (value) {
                        return statCell(farmResolveImg("ui/user/stat-score.png"), value);
                    }
                },
                {
                    field: "coin",
                    title: "金币",
                    width: 84,
                    sortable: true,
                    editor: {type: "numberbox", options: {min: 0, precision: 0}},
                    formatter: function (value) {
                        return statCell(farmResolveImg("ui/user/stat-gold.png"), value);
                    }
                },
                {
                    field: "action",
                    title: "操作",
                    width: 120,
                    formatter: function (value, row, index) {
                        return renderOps(index);
                    }
                },
                {
                    field: "avatarPath",
                    title: "头像路径",
                    width: 10,
                    hidden: true,
                    editor: {type: "textbox"}
                }
            ]],
            onClickRow: function (index) {
                if (state.editIndex >= 0 && state.editIndex !== index) {
                    if (endCurrentEdit()) {
                        $("#userAdminGrid").datagrid("selectRow", index);
                    } else {
                        $("#userAdminGrid").datagrid("selectRow", state.editIndex);
                    }
                }
            },
            onDblClickRow: function (index) {
                beginRowEdit(index);
            },
            onLoadSuccess: function () {
                state.editIndex = -1;
            }
        });
        state.initialized = true;
    }

    function setActive(flag) {
        state.active = !!flag;
        if (state.active) {
            initGrid();
            bindEventsOnce();
            $("#userManagePanel").stop(true, true).css("display", "none").fadeIn(motion().moduleEnterMs, function () {
                $("#userAdminGrid").datagrid("resize", {
                    width: $(".user-admin-grid-panel").width(),
                    height: $(".user-admin-grid-panel").height()
                });
                reload();
            });
            return;
        }
        $("#userManagePanel").stop(true, true).fadeOut(motion().moduleEnterMs);
        $("#userAvatarUploadDialog").dialog("close");
    }

    var bound = false;
    function bindEventsOnce() {
        if (bound) {
            return;
        }
        bindEvents();
        bound = true;
    }

    FarmUserAdminModule.setActive = setActive;
    FarmUserAdminModule.reload = reload;
    window.FarmUserAdminModule = FarmUserAdminModule;
})(window, window.jQuery);
