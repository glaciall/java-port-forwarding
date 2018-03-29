<!DOCTYPE html>
<html lang="en">
<head>
    <#include "./inc/resource.ftl">
    <title>主机管理</title>
</head>
<body>

<#include "./inc/header.ftl">
<div class="container-fluid content">
    <div class="row">
        <#include "./inc/siderbar.ftl">
        <div class="main">
            <div class="row">
                <div class="col-lg-12">
                    <h3 class="page-header"><i class="fa fa-laptop"></i> Dashboard</h3>
                </div>
            </div>
            <div class="row">
                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                            主机列表
                            <button class="btn btn-primary pull-right" id="btn-add"><i class="fa fa-plus"></i> 添加主机</button>
                        </div>
                        <div class="panel-body">
                            <div id="host-table"></div>
                            <ul class="pagination"></ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<#include "./inc/footer.ftl">
<script type="text/html" id="form-add">
    <form method="post" class="form-horizontal">
        <div class="form-group">
            <label class="col-md-3 control-label" for="text-input">主机名称：</label>
            <div class="col-md-9">
                <input type="text" id="name" name="name" class="form-control" placeholder="比如公司电脑">
            </div>
        </div>
        <br>
    </form>
</script>
<script type="text/javascript">
    $(document).ready(function()
    {
        $('#btn-add').click(function()
        {
            modal({
                title : '添加新主机',
                html : $('#form-add').html(),
                close : true,
                ok : function(dialog)
                {
                    var name = $.trim(dialog.find('#name').val());
                    if (name.length == 0) return alert('请填写主机名称'), false;
                    $.post('${context}/manage/host/add', { name : name }, function(result)
                    {
                        if (result.error.code != 0) return alert(result.error.reason);
                        $('#host-table').paginate('reload');
                    });
                }
            });
        });

        $('#host-table').paginate({
            url : '${context}/manage/host/json',
            paginate : $('.pagination'),
            fields : [
                {
                    title : '主机ID',
                    name : 'id',
                    width : '100',
                    align : 'center',
                    formatter : function(i, v, r)
                    {
                        return v;
                    }
                },
                {
                    title : '名称',
                    name : 'name',
                    formatter : function(i, v, r)
                    {
                        return '<i class="fa fa-desktop ' + (r.online ? 'text-primary' : '') + '"></i> ' + v;
                    }
                },
                {
                    title : 'IP',
                    name : 'ip',
                    width : '140',
                    formatter : function(i, v, r)
                    {
                        return v == null || typeof(v) == 'undefined' ? '--' : v;
                    }
                },
                {
                    title : '访问令牌',
                    width : '400',
                    align : 'center',
                    name : 'accesstoken',
                    formatter : function(i, v, r)
                    {
                        return '<span style="font-family: Consolas">' + v + '</span>';
                    }
                },
                {
                    title : '操作',
                    name : 'id',
                    align : 'center',
                    width : '200',
                    formatter : function(i, v, r)
                    {
                        var shtml = '';
                        shtml += '<div class="btn-group" x-host-id="' + v + '">';
                        shtml += '  <a href="${context}/manage/port?hostId=' + v + '" class="btn btn-primary">端口转发</a>';
                        shtml += '  <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown">';
                        shtml += '      <span class="caret"></span>';
                        shtml += '      <span class="sr-only">Toggle Dropdown</span>';
                        shtml += '  </button>';
                        shtml += '  <ul class="dropdown-menu" role="menu">';
                        // TODO: 正式上线时需要移除
                        shtml += '      <li><a href="javascript:;" x-action="edit">修改名称</a></li>';
                        shtml += '      <li><a href="javascript:;" x-action="remove">删除</a></li>';
                        shtml += '      <li><a href="javascript:;" x-action="renew">重置令牌</a></li>';
                        shtml += '  </ul>';
                        shtml += '</div>';
                        return shtml;
                    }
                }
            ]
        });

        $(document).on('click', '.dropdown-menu li a', function()
        {
            var action = $(this).attr('x-action');
            var id = $(this).parents('.btn-group').attr('x-host-id');
            if (window[action] && typeof(window[action]) == 'function') window[action](id);
        });
    });

    function renew(id)
    {
        modal({
            title : '操作提示',
            text : '真的要重置此主机的令牌吗？',
            close : true,
            ok : function()
            {
                $.post('${context}/manage/host/renew', { id : id }, function(result)
                {
                    if (result.error.code != 0) return greeting(result.error.reason);
                    $('#host-table').paginate('reload');
                });
            }
        });
    }

    function edit(id)
    {
        modal({
            title : '操作提示',
            html : $('#form-add').html(),
            close : true,
            ok : function(dialog)
            {
                var name = $.trim(dialog.find('#name').val());
                if (name.length == 0 || name.length > 20) return greeting('请输入主机名称，最多20个字'), false;
                $.post('${context}/manage/host/rename', { id : id, name : name }, function(result)
                {
                    if (result.error.code != 0) return greeting('操作失败：' + result.error.reason);
                    greeting('修改成功');
                    $('#host-table').paginate('reload');
                });
            }
        });
    }

    function remove(id)
    {
        modal({
            title : '操作提示',
            text : '真的要删除此主机吗？该操作将不能恢复！',
            close : true,
            ok : function(dialog)
            {
                $.post('${context}/manage/host/remove', { id : id }, function(result)
                {
                    if (result.error.code != 0) return greeting('操作失败：' + result.error.reason);
                    greeting('操作成功');
                    $('#host-table').paginate('reload');
                });
            }
        });
    }

</script>
</body>
</html>