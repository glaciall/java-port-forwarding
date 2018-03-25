<!DOCTYPE html>
<html lang="en">
<head>
    <#include "./inc/resource.ftl">
    <title>主机端口映射管理</title>
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
                            主机端口映射列表
                            <button class="btn btn-primary pull-right" id="btn-add"><i class="fa fa-plus"></i> 添加映射</button>
                        </div>
                        <div class="panel-body">
                            <div id="port-table"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<#include "./inc/footer.ftl">
<script type="text/html" id="form-add">
    <div class="alert alert-info">
        服务器将会在<strong>代理服务端口</strong>开启监听，当你的客户端连接到服务器的此端口时，服务器将向主机端下发转发指令，主机端将连接到<strong>被代理主机</strong>的<strong>被代理端口</strong>上，同时将客户端与服务器端的连接对应起来，完成数据包的转发代理。
    </div>
    <form method="post" class="form-horizontal">
        <div class="form-group">
            <label class="col-md-3 control-label" for="text-input">代理服务端口：</label>
            <div class="col-md-9">
                <input type="text" id="name" name="name" class="form-control" placeholder="1 ~ 65535">
            </div>
        </div>
        <div class="form-group">
            <label class="col-md-3 control-label" for="text-input">被代理主机：</label>
            <div class="col-md-9">
                <input type="text" id="name" name="name" class="form-control" placeholder="ip 或 域名" value="localhost" />
            </div>
        </div>
        <div class="form-group">
            <label class="col-md-3 control-label" for="text-input">被代理端口：</label>
            <div class="col-md-9">
                <input type="text" id="name" name="name" class="form-control" placeholder="如远程桌面的3389端口" />
            </div>
        </div>
        <div class="form-group">
            <label class="col-md-3 control-label" for="text-input">网络IO超时：</label>
            <div class="col-md-9">
                <input type="text" id="name" name="name" class="form-control" placeholder="多长时间未读取到数据就断开连接（单位：秒）" value="30" />
            </div>
        </div>
        <div class="form-group">
            <label class="col-md-3 control-label" for="text-input">连接超时：</label>
            <div class="col-md-9">
                <input type="text" id="name" name="name" class="form-control" placeholder="主机端多长时间未响应就断开连接（单位：秒）" value="30" />
            </div>
        </div>
        <div class="form-group">
            <label class="col-md-3 control-label" for="text-input">最大并发连接：</label>
            <div class="col-md-9">
                <input type="text" id="name" name="name" class="form-control" value="10" />
            </div>
        </div>
    </form>
</script>
<script type="text/javascript">
    $(document).ready(function()
    {
        $('#btn-add').click(function()
        {
            modal({
                title : '添加新映射',
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

        $('#port-table').paginate({
            url : '${context}/manage/port/json?hostId=${hostId}',
            paginate : $('.pagination'),
            fields : [
                {
                    title : '#',
                    name : 'id',
                    align : 'center',
                    formatter : function(i, v, r)
                    {
                        return i + 1;
                    }
                },
                {
                    title : '代理服务端口',
                    name : 'listenPort',
                    align : 'right',
                },
                {
                    title : '被代理主机',
                    name : 'hostIp',
                },
                {
                    title : '被代理端口',
                    name : 'hostPort',
                    align : 'right',
                },
                {
                    title : '网络IO超时',
                    name : 'soTimeout',
                    align : 'right',
                },
                {
                    title : '连接超时',
                    name : 'connectTimeout',
                    align : 'right',
                },
                {
                    title : '最大并发',
                    name : 'concurrentConnections',
                    align : 'right',
                },
                {
                    title : '当前并发',
                    name : 'id',
                    align : 'right',
                    formatter : function(i, v, r)
                    {
                        return '--';
                    }
                },
                {
                    title : '状态',
                    name : 'state',
                    align : 'center',
                    formatter : function(i, v, r)
                    {
                        return ['--', '<span class="text-primary">正常</span>', '<span class="text-muted">禁用</span>'][v];
                    }
                },
                {
                    title : '最近连接时间',
                    name : 'lastActiveTime',
                    align : 'center',
                    formatter : function(i, v, r)
                    {
                        if (v == 0) return '--';
                        return new Date(v).format('yyyy-MM-dd hh:mm:ss');
                    }
                },
                {
                    title : '操作',
                    name : 'id',
                    align : 'center',
                    formatter : function(i, v, r)
                    {
                        var shtml = '';
                        shtml += '<div class="btn-group" x-host-id="' + v + '">';
                        if (r.state == 1)
                        shtml += '  <a href="javascript:disable(' + v + ');" class="btn btn-primary">停用</a>';
                        if (r.state == 2)
                        shtml += '  <a href="javascript:enable(' + v + ');" class="btn btn-primary">启用</a>';
                        shtml += '  <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown">';
                        shtml += '      <span class="caret"></span>';
                        shtml += '      <span class="sr-only">Toggle Dropdown</span>';
                        shtml += '  </button>';
                        shtml += '  <ul class="dropdown-menu" role="menu">';
                        // TODO: 正式上线时需要移除
                        shtml += '      <li><a href="javascript:;" x-action="edit">修改参数</a></li>';
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

    function enable(id)
    {
        greeting('要启用它');
    }

    function disable(id)
    {
        greeting('要禁用它');
    }

    function edit(id)
    {
        greeting('要修改它');
    }

</script>
</body>
</html>