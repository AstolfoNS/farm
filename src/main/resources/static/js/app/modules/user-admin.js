(function (window, $) {
    var FarmUserAdminModule = {};
    var state = {
        active: false,
        initialized: false,
        editIndex: -1,
        uploadRowIndex: -1
    };
    var bound = false;

    function motion() {
        if (window.FarmUi && $.isFunction(window.FarmUi.motion)) {
            return window.FarmUi.motion();
        }
        if ($.isFunction(window.farmMotion)) {
            return window.farmMotion();
        }
        return {moduleEnterMs: 260, actionFeedbackMs: 1200};
    }

    function asNumber(value, def) {
        if (window.FarmUi && $.isFunction(window.FarmUi.asNumber)) {
            return window.FarmUi.asNumber(value, def);
        }
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
        return (window.farmDefaultAsset && window.farmDefaultAsset("avatar")) || "";
    }

    function trimText(value) {
        return $.trim(value == null ? "" : String(value));
    }

    function displayUserName(row) {
        var data = row || {};
        var nickname = trimText(data.nickname);
        var username = trimText(data.username);
        if (nickname && username) {
            return nickname + "[" + username + "]";
        }
        return nickname || username || "当前用户";
    }

    function buildUserSaveMessage(res, row) {
        var data = (res && res.data) || row || {};
        return "用户资料已保存：" + displayUserName(data) +
            "。\n经验 " + asNumber(data.experience, 0) +
            "，积分 " + asNumber(data.score, 0) +
            "，金币 " + asNumber(data.coin, 0) +
            "，列表与顶部当前用户信息会同步刷新。";
    }

    function buildUserDeleteMessage(row) {
        return "用户已删除：" + displayUserName(row) +
            "。\n该记录会从当前列表移除；如果它正是当前选中用户，请重新选择新的当前用户。";
    }

    function buildAvatarUploadMessage(res, row) {
        var data = (res && res.data) || {};
        var filePath = trimText(data.relativePath || data.path || "");
        return "头像上传已完成：" + displayUserName(row) +
            "。\n文件路径已回填到当前编辑行" +
            (filePath ? "，资源路径为 " + filePath : "") +
            "，请继续点击“保存数据”以正式写入用户资料。";
    }

    function buildUserSaveFailMessage(res, row) {
        var reason = trimText(res && res.msg) || "后端未返回明确错误";
        return "用户资料未保存：" + displayUserName(row) +
            "。\n失败原因：" + reason + "。";
    }

    function buildUserDeleteFailMessage(res, row) {
        var reason = trimText(res && res.msg) || "后端未返回明确错误";
        return "用户删除未完成：" + displayUserName(row) +
            "。\n失败原因：" + reason + "。";
    }

    function buildAvatarUploadFailMessage(res, row) {
        var reason = trimText(res && res.msg) || "文件上传或 URL 回填未通过校验";
        return "头像上传未完成：" + displayUserName(row) +
            "。\n失败原因：" + reason + "。";
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

    function showMessage(res, options) {
        var opts = options || {};
        var ok = FarmApi.isOk(res);
        var text = (res && res.msg) ? res.msg : (ok ? "操作成功" : "操作失败");
        if (ok && $.isFunction(opts.successBuilder)) {
            text = opts.successBuilder(res) || text;
        }
        if (!ok && $.isFunction(opts.failBuilder)) {
            text = opts.failBuilder(res) || text;
        }
        if (ok) {
            $.messager.show({
                title: opts.successTitle || "提示",
                msg: text,
                timeout: motion().actionFeedbackMs,
                showType: "slide"
            });
            return;
        }
        $.messager.alert(opts.failTitle || "提示", text);
    }

    function reload() {
        if (!state.initialized) {
            return;
        }
        state.editIndex = -1;
        if (window.FarmGrid && $.isFunction(window.FarmGrid.reload)) {
            window.FarmGrid.reload("#userAdminGrid", false);
            return;
        }
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
            head: (window.farmDefaultAsset && window.farmDefaultAsset("avatar")) || "",
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
                showMessage(res, {
                    successBuilder: function () {
                        return buildUserDeleteMessage(row);
                    },
                    failBuilder: function (response) {
                        return buildUserDeleteFailMessage(response, row);
                    }
                });
                if (FarmApi.isOk(res)) {
                    reload();
                }
            }, function () {
                $.messager.alert("提示", buildUserDeleteFailMessage(null, row));
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
            showMessage(res, {
                successBuilder: function (response) {
                    return buildUserSaveMessage(response, row);
                },
                failBuilder: function (response) {
                    return buildUserSaveFailMessage(response, row);
                }
            });
            if (!FarmApi.isOk(res)) {
                return;
            }
            reload();
            if (window.FarmHomeBridge && $.isFunction(window.FarmHomeBridge.refreshCurUser)) {
                window.FarmHomeBridge.refreshCurUser();
            }
            if (window.FarmHomeBridge && $.isFunction(window.FarmHomeBridge.reloadUserOptions)) {
                window.FarmHomeBridge.reloadUserOptions();
            }
        }, function () {
            $.messager.alert("提示", buildUserSaveFailMessage(null, row));
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
            $.messager.show({
                title: "提示",
                msg: "请先选择要上传头像的用户",
                timeout: motion().actionFeedbackMs,
                showType: "slide"
            });
            return;
        }
        if (!$("#userAvatarFile").val()) {
            $.messager.show({
                title: "提示",
                msg: "请选择需要上传的头像文件",
                timeout: motion().actionFeedbackMs,
                showType: "slide"
            });
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
                var rows = $("#userAdminGrid").datagrid("getRows");
                var currentRow = rows[state.uploadRowIndex] || null;
                showMessage(res, {
                    successBuilder: function (response) {
                        return buildAvatarUploadMessage(response, currentRow);
                    },
                    failBuilder: function (response) {
                        return buildAvatarUploadFailMessage(response, currentRow);
                    }
                });
                if (!FarmApi.isOk(res) || !res.data) {
                    return;
                }
                var avatarPath = res.data.relativePath || res.data.path || "";
                var accessUrl = res.data.accessUrl || "";
                updateAvatarToRow(state.uploadRowIndex, avatarPath, accessUrl);
                $("#userAvatarUploadDialog").dialog("close");
            },
            error: function () {
                var rows = $("#userAdminGrid").datagrid("getRows");
                var currentRow = rows[state.uploadRowIndex] || null;
                $.messager.show({
                    title: "提示",
                    msg: buildAvatarUploadFailMessage(null, currentRow),
                    timeout: motion().actionFeedbackMs,
                    showType: "slide"
                });
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

        $("#userAdminSearchBtn")
            .off("click.userAdmin")
            .on("click.userAdmin", function () {
                endCurrentEdit();
                $("#userAdminGrid").datagrid("load", {page: 1});
            });

        $("#userAdminName").textbox("textbox")
            .off("keydown.userAdmin")
            .on("keydown.userAdmin", function (event) {
                if (event.keyCode === 13) {
                    $("#userAdminSearchBtn").trigger("click");
                }
            });

        $("#userAdminAddBtn").off("click.userAdmin").on("click.userAdmin", addUserRow);
        $("#userAdminCancelBtn").off("click.userAdmin").on("click.userAdmin", cancelEdit);
        $("#userAdminDeleteBtn").off("click.userAdmin").on("click.userAdmin", deleteSelectedUser);
        $("#userAdminCloseBtn")
            .off("click.userAdmin")
            .on("click.userAdmin", function () {
                if (window.FarmHomeBridge && $.isFunction(window.FarmHomeBridge.switchModule)) {
                    window.FarmHomeBridge.switchModule("home");
                }
            });

        $("#userManagePanel")
            .off("click.userAdmin", ".user-admin-op")
            .on("click.userAdmin", ".user-admin-op", function () {
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

        $("#userAvatarUploadBtn").off("click.userAdmin").on("click.userAdmin", uploadAvatar);
        $("#userAvatarCloseBtn")
            .off("click.userAdmin")
            .on("click.userAdmin", function () {
                $("#userAvatarUploadDialog").dialog("close");
            });
    }

    function initGrid() {
        if (state.initialized) {
            return;
        }
        var loader = (window.FarmGrid && $.isFunction(window.FarmGrid.buildRemoteLoader))
            ? window.FarmGrid.buildRemoteLoader({
                request: function (param, onSuccess, onError) {
                    FarmApi.userAdminPage(pagePayload(param), onSuccess, onError);
                },
                resolve: function (res) {
                    if (!FarmApi.isOk(res) || !res.data) {
                        return {total: 0, rows: []};
                    }
                    var pageRows = [];
                    if ($.isArray(res.data.records)) {
                        pageRows = res.data.records;
                    } else if ($.isArray(res.data.rows)) {
                        pageRows = res.data.rows;
                    }
                    return {
                        total: asNumber(res.data.total, 0),
                        rows: pageRows
                    };
                }
            })
            : null;

        var options = {
            fit: false,
            width: "100%",
            height: 422,
            pagination: true,
            pageSize: 5,
            pageList: [5, 10, 20],
            idField: "id",
            loader: loader || function (param, success, error) {
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
                        return statCell(farmResolveImg("app/user/stat-exp.png"), value);
                    }
                },
                {
                    field: "score",
                    title: "积分",
                    width: 76,
                    sortable: true,
                    editor: {type: "numberbox", options: {min: 0, precision: 0}},
                    formatter: function (value) {
                        return statCell(farmResolveImg("app/user/stat-score.png"), value);
                    }
                },
                {
                    field: "coin",
                    title: "金币",
                    width: 84,
                    sortable: true,
                    editor: {type: "numberbox", options: {min: 0, precision: 0}},
                    formatter: function (value) {
                        return statCell(farmResolveImg("app/user/stat-gold.png"), value);
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
        };
        if (window.FarmGrid && $.isFunction(window.FarmGrid.init)) {
            window.FarmGrid.init("#userAdminGrid", options);
        } else {
            $("#userAdminGrid").datagrid(options);
        }
        state.initialized = true;
    }

    function bindEventsOnce() {
        if (bound) {
            return;
        }
        bindEvents();
        bound = true;
    }

    function setActive(flag) {
        state.active = !!flag;
        if (state.active) {
            initGrid();
            bindEventsOnce();
            if (window.FarmUi && $.isFunction(window.FarmUi.showPanel)) {
                window.FarmUi.showPanel($("#userManagePanel"));
            } else {
                $("#userManagePanel").stop(true, true).css("display", "none").fadeIn(motion().moduleEnterMs);
            }
            window.setTimeout(function () {
                $("#userAdminGrid").datagrid("resize", {
                    width: $(".user-admin-grid-panel").width(),
                    height: $(".user-admin-grid-panel").height()
                });
                reload();
            }, motion().moduleEnterMs + 20);
            return;
        }
        if (window.FarmUi && $.isFunction(window.FarmUi.hidePanel)) {
            window.FarmUi.hidePanel($("#userManagePanel"));
        } else {
            $("#userManagePanel").stop(true, true).fadeOut(motion().moduleEnterMs);
        }
        $("#userAvatarUploadDialog").dialog("close");
    }

    FarmUserAdminModule.setActive = setActive;
    FarmUserAdminModule.reload = reload;
    window.FarmUserAdminModule = FarmUserAdminModule;
    if (window.FarmCore && $.isFunction(window.FarmCore.registerSetActiveModule)) {
        window.FarmCore.registerSetActiveModule("user-manage", FarmUserAdminModule, {refreshMethod: "reload"});
    }
})(window, window.jQuery);
