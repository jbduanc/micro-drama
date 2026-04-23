package com.tcm.rx.service.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.tcm.common.constants.CommonConstants;
import com.tcm.common.core.UserContextHolder;
import com.tcm.common.entity.BaseUser;
import com.tcm.common.enums.BooleanEnum;
import com.tcm.common.exception.ServiceException;
import com.tcm.rx.entity.auth.CMenu;
import com.tcm.rx.entity.auth.CUser;
import com.tcm.rx.mapper.auth.CMenuMapper;
import com.tcm.rx.service.auth.ICMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcm.rx.service.auth.ICUserService;
import com.tcm.rx.vo.auth.request.MenuQueryVO;
import com.tcm.rx.vo.auth.response.CMenuVO;
import com.tcm.rx.vo.auth.response.MetaVO;
import com.tcm.rx.vo.auth.response.RouterVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 诊疗开方系统--菜单表 服务实现类
 * </p>
 *
 * @author xph
 * @since 2025-07-08
 */
@Service
public class CMenuServiceImpl extends ServiceImpl<CMenuMapper, CMenu> implements ICMenuService {

    @Resource
    private ICUserService cUserService;

    @Override
    public List<CMenuVO> menuList(MenuQueryVO queryVO) {
        return this.getBaseMapper().menuList(queryVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertMenu(CMenuVO menuVO) {
        if (Objects.isNull(menuVO)) {
            throw new ServiceException("参数不能为空");
        }
        /*if (Objects.isNull(menuVO.getMedicalGroupId()) && StringUtils.isBlank(menuVO.getMedicalGroupCode())) {
            throw new ServiceException("医联体参数不能为空");
        }
        if (Objects.isNull(menuVO.getRxId()) && StringUtils.isBlank(menuVO.getRxCode())) {
            throw new ServiceException("诊疗系统参数不能为空");
        }*/

        CMenu menu = new CMenu();
        BeanUtils.copyProperties(menuVO, menu);

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        menu.setCreateBy(loginUser.getId().toString());
        menu.setUpdateBy(loginUser.getId().toString());
        // 直接取当前用户的类型：P.集团Saas系统管理，B.煎药系统，C.诊疗系统
//        menu.setDataSource(loginUser.getPlatformType());

        Date now = new Date();
        menu.setCreateTime(now);
        menu.setUpdateTime(now);

        int insertResult = this.getBaseMapper().insert(menu);
        return insertResult > 0 ? menu.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long updateMenu(CMenuVO menuVO) {
        if (Objects.isNull(menuVO) || Objects.isNull(menuVO.getId())) {
            throw new ServiceException("参数不能为空");
        }

        CMenu menu = new CMenu();
        BeanUtils.copyProperties(menuVO, menu);

        BaseUser loginUser = UserContextHolder.getUserInfoContext();
        menu.setUpdateBy(loginUser.getId().toString());

        menu.setUpdateTime(new Date());
        return this.saveOrUpdate(menu) ? menu.getId() : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        if (Objects.isNull(id) || id <= 0){
            throw new ServiceException("id参数不能为空");
        }

        CMenu menu = this.getById(id);
        if (ObjectUtils.isEmpty(menu)){
            throw new ServiceException("菜单信息不存在");
        }

        this.getBaseMapper().deleteById(id);
    }

    @Override
    public List<CMenuVO> menuByRole(MenuQueryVO queryVO) {
        return this.getBaseMapper().menuByRole(queryVO);
    }

    @Override
    public List<CMenuVO> menuByUser(MenuQueryVO queryVO) {
        return this.getBaseMapper().menuByUser(queryVO);
    }

    @Override
    public List<CMenuVO> menuTreeByUser(MenuQueryVO queryVO) {
        List<CMenuVO> result = Lists.newArrayList();
        if (Objects.isNull(queryVO) || Objects.isNull(queryVO.getUserId()) || queryVO.getUserId() < 1){
            return result;
        }

        CUser user = cUserService.getById(queryVO.getUserId());
        if (Objects.isNull(user)){
            return result;
        }

        List<String> menuTypes = Lists.newArrayList("M", "C", "F");
        if (user.getUserType().equals("admin")){
            LambdaQueryWrapper<CMenu> queryWrapper = new LambdaQueryWrapper<CMenu>()
                    .in(CMenu::getMenuType, menuTypes)
//                    .eq(Objects.nonNull(user.getMedicalGroupId()),
//                            CMenu::getMedicalGroupId, user.getMedicalGroupId())
//                    .eq(StringUtils.isNotBlank(user.getMedicalGroupCode()),
//                            CMenu::getMedicalGroupCode, user.getMedicalGroupCode())
//                    .eq(Objects.nonNull(user.getRxId()), CMenu::getRxId, user.getRxId())
//                    .eq(StringUtils.isNotBlank(user.getRxCode()), CMenu::getRxCode, user.getRxCode())
                    .eq(CMenu::getStatus, BooleanEnum.TRUE.getNum())
                    .orderByAsc(CMenu::getParentId)
                    .orderByAsc(CMenu::getSort);
            List<CMenu> dbList = this.list(queryWrapper);
            if (CollectionUtils.isNotEmpty(dbList)){
                for (CMenu menu : dbList){
                    CMenuVO vo = new CMenuVO();
                    BeanUtils.copyProperties(menu,vo);
                    result.add(vo);
                }
            }
        } else {
            queryVO.setStatus(BooleanEnum.TRUE.getNum());
            queryVO.setMenuTypes(menuTypes);
            result = this.getBaseMapper().menuByUser(queryVO);
        }

        // 去重
        if (CollectionUtils.isNotEmpty(result)) {
            result = Lists.newArrayList(result
                    .stream()
                    .collect(Collectors.toMap(CMenuVO::getId, Function.identity(), (k1, k2) -> k1))
                    .values());
        }

        return this.getChildMenus(result, 0);
    }

    @Override
    public List<RouterVO> buildMenus(List<CMenuVO> menuList) {
        List<RouterVO> routers = new LinkedList<>();
        if (CollectionUtils.isEmpty(menuList)){
            return routers;
        }

        for (CMenuVO menu : menuList) {
            RouterVO router = new RouterVO();
            router.setHidden(BooleanEnum.TRUE.getNum().equals(menu.getVisible()));
            router.setName(this.getRouteName(menu));
            router.setPath(this.getRouterPath(menu));
            router.setComponent(this.getComponent(menu));
            router.setQuery(menu.getQuery());
            router.setMeta(new MetaVO(){{
                setTitle(menu.getMenuName());
                setIcon(menu.getIcon());
                setNoCache(BooleanEnum.TRUE.getNum().equals(menu.getIsCache()));
                setLink(menu.getPath());
            }});

            List<CMenuVO> cMenus = menu.getChildren();
            if (CollectionUtils.isNotEmpty(cMenus) && CommonConstants.SysAuthConstants.TYPE_DIR.equals(menu.getMenuType())) {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(cMenus));
            } else if (this.isMenuFrame(menu)) {
                router.setMeta(null);
                List<RouterVO> childrenList = Lists.newArrayList();
                RouterVO children = new RouterVO();
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());
                children.setName(org.apache.commons.lang.StringUtils.capitalize(menu.getPath()));
                children.setMeta(new MetaVO(){{
                    setTitle(menu.getMenuName());
                    setIcon(menu.getIcon());
                    setNoCache(BooleanEnum.TRUE.getNum().equals(menu.getIsCache()));
                    setLink(menu.getPath());
                }});
                children.setQuery(menu.getQuery());
                childrenList.add(children);
                router.setChildren(childrenList);
            } else if (menu.getParentId().intValue() == 0 && this.isInnerLink(menu)) {
                router.setMeta(new MetaVO(){{
                    setTitle(menu.getMenuName());
                    setIcon(menu.getIcon());
                }});
                router.setPath("/");
                List<RouterVO> childrenList = Lists.newArrayList();
                RouterVO children = new RouterVO();
                String routerPath = this.innerLinkReplaceEach(menu.getPath());
                children.setPath(routerPath);
                children.setComponent(CommonConstants.SysAuthConstants.INNER_LINK);
                children.setName(org.apache.commons.lang.StringUtils.capitalize(routerPath));
                children.setMeta(new MetaVO(){{
                    setTitle(menu.getMenuName());
                    setIcon(menu.getIcon());
                    setLink(menu.getPath());
                }});
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list     分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    private List<CMenuVO> getChildMenus(List<CMenuVO> list, int parentId) {
        List<CMenuVO> returnList = Lists.newArrayList();
        for (CMenuVO t : list) {
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     *
     * @param list
     * @param t
     */
    private void recursionFn(List<CMenuVO> list, CMenuVO t) {
        // 得到子节点列表
        List<CMenuVO> childList = this.getChildList(list, t);
        t.setChildren(childList);
        for (CMenuVO tChild : childList) {
            if (this.hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<CMenuVO> getChildList(List<CMenuVO> list, CMenuVO t) {
        List<CMenuVO> tlist = Lists.newArrayList();
        for (CMenuVO n : list) {
            if (n.getParentId().longValue() == t.getId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<CMenuVO> list, CMenuVO t) {
        return !this.getChildList(list, t).isEmpty();
    }

    /*------------------------------------------------------------------------------------------*/

    /**
     * 获取路由名称
     *
     * @param menu 菜单信息
     * @return 路由名称
     */
    private String getRouteName(CMenuVO menu) {
        String routerName = org.apache.commons.lang.StringUtils.capitalize(menu.getPath());
        // 非外链并且是一级目录（类型为目录）
        if (this.isMenuFrame(menu)) {
            routerName = "";
        }
        return routerName;
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    private String getRouterPath(CMenuVO menu) {
        String routerPath = menu.getPath();
        // 内链打开外网方式
        if (menu.getParentId().intValue() != 0 && this.isInnerLink(menu)) {
            routerPath = this.innerLinkReplaceEach(routerPath);
        }
        // 非外链并且是一级目录（类型为目录）
        if (0 == menu.getParentId().intValue() && CommonConstants.SysAuthConstants.TYPE_DIR.equals(menu.getMenuType())
                && CommonConstants.SysAuthConstants.NO_FRAME.equals(menu.getIsFrame())) {
            routerPath = "/" + menu.getPath();
        }
        // 非外链并且是一级目录（类型为菜单）
        else if (this.isMenuFrame(menu)) {
            routerPath = "/";
        }
        return routerPath;
    }

    /**
     * 获取组件信息
     *
     * @param menu 菜单信息
     * @return 组件信息
     */
    private String getComponent(CMenuVO menu) {
        String component = CommonConstants.SysAuthConstants.LAYOUT;
        if (org.apache.commons.lang.StringUtils.isNotEmpty(menu.getComponent()) && !this.isMenuFrame(menu)) {
            component = menu.getComponent();
        } else if (org.apache.commons.lang.StringUtils.isEmpty(menu.getComponent()) && menu.getParentId().intValue() != 0 && this.isInnerLink(menu)) {
            component = CommonConstants.SysAuthConstants.INNER_LINK;
        } else if (org.apache.commons.lang.StringUtils.isEmpty(menu.getComponent()) && this.isParentView(menu)) {
            component = CommonConstants.SysAuthConstants.PARENT_VIEW;
        }
        return component;
    }

    /**
     * 是否为菜单内部跳转
     *
     * @param menu 菜单信息
     * @return 结果
     */
    private boolean isMenuFrame(CMenuVO menu) {
        return menu.getParentId().intValue() == 0 && CommonConstants.SysAuthConstants.TYPE_MENU.equals(menu.getMenuType())
                && menu.getIsFrame().equals(CommonConstants.SysAuthConstants.NO_FRAME);
    }

    /**
     * 是否为内链组件
     *
     * @param menu 菜单信息
     * @return 结果
     */
    private boolean isInnerLink(CMenuVO menu) {

        return menu.getIsFrame().equals(CommonConstants.SysAuthConstants.NO_FRAME) && org.apache.commons.lang3.StringUtils.startsWithAny(menu.getPath(), "http://", "https://");
    }

    /**
     * 是否为parent_view组件
     *
     * @param menu 菜单信息
     * @return 结果
     */
    private boolean isParentView(CMenuVO menu) {
        return menu.getParentId().intValue() != 0 && CommonConstants.SysAuthConstants.TYPE_DIR.equals(menu.getMenuType());
    }

    /**
     * 内链域名特殊字符替换
     * @return
     */
    private String innerLinkReplaceEach(String path) {
        return org.apache.commons.lang.StringUtils.replaceEach(path, new String[]{CommonConstants.SysAuthConstants.HTTP, CommonConstants.SysAuthConstants.HTTPS, CommonConstants.SysAuthConstants.WWW, "."},
                new String[]{"", "", "", "/"});
    }

}
