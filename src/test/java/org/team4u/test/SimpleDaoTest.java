package org.team4u.test;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.RandomUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.team4u.dao.core.Dao;
import org.team4u.dao.core.SimpleDao;
import org.team4u.dao.transaction.Transactions;
import org.team4u.kit.core.action.Callback;
import org.team4u.kit.core.action.Function;
import org.team4u.kit.core.lang.Pager;
import org.team4u.kit.core.util.CollectionExUtil;
import org.team4u.sql.builder.Sql;
import org.team4u.sql.builder.entity.builder.EntitySelectSqlBuilder;
import org.team4u.sql.builder.util.SqlBuilders;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.team4u.test.TestUtil.createAndInitDao;
import static org.team4u.test.TestUtil.initSqlContent;

/**
 * @author Jay Wu
 */
public class SimpleDaoTest {

    private static SimpleDao dao;

    @BeforeClass
    public static void before() {
        dao = createAndInitDao();
    }

    @Test
    public void queryForObject() {
        TestEntity entity = insertEntity(dao);
        EntitySelectSqlBuilder sqlBuilder = SqlBuilders.select(TestEntity.class)
                .where("id", "=", entity.getId());

        Sql sql = sqlBuilder.create();
        Assert.assertEquals(entity, dao.queryForObject(sqlBuilder));

        Assert.assertEquals(new TestBean(entity), dao.queryForObject(TestBean.class, sql));

        Dict map = dao.queryForObject(Dict.class, sql);
        Assert.assertEquals(entity.getId(), map.getStr("client_id"));
        Assert.assertEquals(entity.getName(), map.getStr("name"));

        sql = SqlBuilders.select(TestEntity.class)
                .column("name")
                .where("id", "=", entity.getId())
                .create();
        String name = dao.queryForObject(String.class, sql);
        Assert.assertEquals(entity.getName(), name);

        Assert.assertEquals(entity, dao.queryWithPkForObject(TestEntity.class, entity.getId()));

        Assert.assertEquals(entity, dao.queryForObject(TestEntity.class,
                SqlBuilders.sql("select * from client x where client_id = :id")
                        .setParameter("id", entity.getId())
                        .create()));
    }

    @Test
    public void queryForList() {
        TestEntity entity1 = insertEntity(dao);
        EntitySelectSqlBuilder<TestEntity> sqlBuilder = SqlBuilders.select(TestEntity.class)
                .where("id", "=", entity1.getId());
        List<TestEntity> entities = dao.queryForList(sqlBuilder);
        Assert.assertEquals(1, entities.size());
        Assert.assertTrue(entities.contains(entity1));

        TestEntity entity2 = insertEntity(dao);
        sqlBuilder = SqlBuilders.select(TestEntity.class).where("name", "=", entity1.getName());

        Sql sql = sqlBuilder.create();
        Pager pager = new Pager().setPageSize(1).setPageNumber(1);
        entities = dao.queryForList(sqlBuilder, pager);
        Assert.assertEquals(1, entities.size());
        Assert.assertTrue(entities.contains(entity1) || entities.contains(entity2));
        Assert.assertEquals(0, pager.getRecordCount());

        pager = new Pager().setAutoCount(true);
        entities = dao.queryForList(TestEntity.class, sql, pager);
        Assert.assertEquals(2, entities.size());
        Assert.assertEquals(2, pager.getRecordCount());

        List<TestBean> beans = dao.queryForList(TestBean.class, sql);
        Assert.assertEquals(2, beans.size());

        List<TestBean> expectBeans = CollectionExUtil.collect(entities, new Function<TestEntity, TestBean>() {
            @Override
            public TestBean invoke(TestEntity entity) {
                return new TestBean(entity);
            }
        });
        Assert.assertTrue(expectBeans.containsAll(beans));

        sql = SqlBuilders.select(TestEntity.class)
                .column("id")
                .where("name", "=", "x")
                .create();
        List<String> idList = dao.queryForList(String.class, sql);
        List<String> expectIdList = CollectionExUtil.collect(entities, new Function<TestEntity, String>() {
            @Override
            public String invoke(TestEntity obj) {
                return obj.getId();
            }
        });
        Assert.assertTrue(expectIdList.containsAll(idList));

        Assert.assertEquals(CollectionUtil.newArrayList(entity1), dao.queryForList(TestEntity.class,
                SqlBuilders.sql("select * from client x where client_id = :id")
                        .setParameter("id", entity1.getId())
                        .create()));
    }

    @Test
    public void insert() {
        TestEntity entity = insertEntity(dao);

        TestEntity result = dao.queryWithPkForObject(TestEntity.class, entity.getId());
        Assert.assertEquals(entity.getId(), result.getId());
        Assert.assertEquals(1, dao.delete(result));

        dao.insert(CollectionUtil.newArrayList(entity));
        Assert.assertEquals(1, dao.delete(entity));

        TestTokenEntity tokenEntity = new TestTokenEntity();
        entity.setName("x");
        dao.insert(tokenEntity);
        Assert.assertEquals(1, tokenEntity.getId().longValue());
        Assert.assertEquals(1, dao.delete(tokenEntity));

        tokenEntity.setId(null);
        dao.insert(CollectionUtil.newArrayList(tokenEntity));
        Assert.assertEquals(2, tokenEntity.getId().longValue());
        Assert.assertEquals(1, dao.delete(tokenEntity));

        tokenEntity.setId(null);
        dao.fastInsert(CollectionUtil.newArrayList(tokenEntity));
        Assert.assertNull(tokenEntity.getId());
        tokenEntity.setId(3L);
        Assert.assertEquals(1, dao.delete(tokenEntity));

        entity.setName("1");

        dao.insert(CollectionUtil.newArrayList(entity), "name", false);
        result = dao.queryWithPkForObject(TestEntity.class, entity.getId());
        Assert.assertEquals("1", result.getName());
        Assert.assertEquals(null, result.getRemark());
        Assert.assertEquals(1, dao.delete(entity));

        dao.fastInsert(CollectionUtil.newArrayList(entity), "name", false);
        result = dao.queryWithPkForObject(TestEntity.class, entity.getId());
        Assert.assertEquals("1", result.getName());
        Assert.assertEquals(null, result.getRemark());
        Assert.assertEquals(1, dao.delete(entity));

        entity.setRemark(null);

        dao.insert(CollectionUtil.newArrayList(entity), null, true);
        result = dao.queryWithPkForObject(TestEntity.class, entity.getId());
        Assert.assertEquals("1", result.getName());
        Assert.assertEquals(null, result.getRemark());
        Assert.assertEquals(1, dao.delete(entity));

        dao.fastInsert(CollectionUtil.newArrayList(entity), null, true);
        result = dao.queryWithPkForObject(TestEntity.class, entity.getId());
        Assert.assertEquals("1", result.getName());
        Assert.assertEquals(null, result.getRemark());
        Assert.assertEquals(1, dao.delete(entity));
    }

    @Test
    public void update() {
        TestEntity entity = insertEntity(dao);
        entity.setName(RandomUtil.randomUUID());
        Assert.assertEquals(1, dao.update(entity));
        Assert.assertEquals(entity.getName(),
                dao.queryWithPkForObject(TestEntity.class, entity.getId()).getName());

        entity.setName(RandomUtil.randomUUID());
        Assert.assertEquals(1, dao.update(CollectionUtil.newArrayList(entity))[0]);

        String remark = entity.getRemark();
        entity.setName("1");
        entity.setRemark(null);

        dao.update(CollectionUtil.newArrayList(entity), "name", false);
        TestEntity result = dao.queryWithPkForObject(TestEntity.class, entity.getId());
        Assert.assertEquals("1", result.getName());
        Assert.assertEquals(remark, result.getRemark());

        dao.update(CollectionUtil.newArrayList(entity), null, true);
        result = dao.queryWithPkForObject(TestEntity.class, entity.getId());
        Assert.assertEquals("1", result.getName());
        Assert.assertEquals(remark, result.getRemark());
    }

    @Test
    public void delete() throws SQLException {
        TestEntity entity = insertEntity(dao);
        Assert.assertEquals(1, dao.delete(entity));

        entity = insertEntity(dao);
        Assert.assertEquals(1, dao.deleteWithPK(entity.getClass(), entity.getId()));

        entity = insertEntity(dao);
        Assert.assertEquals(1, dao.delete(CollectionUtil.newArrayList(entity))[0]);

        entity = insertEntity(dao);
        Sql sql = SqlBuilders.delete(TestEntity.class)
                .where("id", "=", entity.getId())
                .create();
        Assert.assertEquals(1, dao.execute(sql));
    }

    @Test
    public void transaction() {
        final TestEntity[] entity = new TestEntity[2];
        Transactions.execute(new Runnable() {
            @Override
            public void run() {
                entity[0] = insertEntity(dao);

                Transactions.execute(new Runnable() {
                    @Override
                    public void run() {
                        entity[1] = insertEntity(dao);
                    }
                });
            }
        });

        Assert.assertNull(Transactions.get());
        Assert.assertEquals(1, dao.delete(entity[0]));
        Assert.assertEquals(1, dao.delete(entity[1]));

        entity[0] = null;

        try {
            Transactions.execute(new Runnable() {
                @Override
                public void run() {
                    entity[0] = insertEntity(dao);

                    Transactions.execute(new Runnable() {
                        @Override
                        public void run() {
                            entity[1] = insertEntity(dao);
                            throw new RuntimeException();
                        }
                    });
                }
            });

            Assert.fail();
        } catch (RuntimeException e) {
            // Ignore error
        }

        Assert.assertNull(Transactions.get());
        Assert.assertEquals(0, dao.delete(entity[0]));
        Assert.assertEquals(0, dao.delete(entity[1]));
    }

    @Test
    public void each() {
        final List<TestEntity> entities = CollectionUtil.newArrayList(insertEntity(dao), insertEntity(dao));
        final List<TestEntity> entitiesResult = CollectionUtil.newArrayList();

        EntitySelectSqlBuilder<TestEntity> sqlBuilder = SqlBuilders.select(TestEntity.class).where("name", "=", "x");
        Sql sql = sqlBuilder.create();
        dao.each(sqlBuilder, new Callback<TestEntity>() {
            @Override
            public void invoke(TestEntity obj) {
                entitiesResult.add(obj);
            }
        });
        Assert.assertTrue(entitiesResult.containsAll(entities));

        final List<TestBean> beans = dao.queryForList(TestBean.class, sql);
        final List<TestBean> beanResults = CollectionUtil.newArrayList();

        dao.each(TestBean.class, sql, new Callback<TestBean>() {
            @Override
            public void invoke(TestBean obj) {
                beanResults.add(obj);
            }
        });
        Assert.assertTrue(beanResults.containsAll(beans));

        final List<Dict> mapList = dao.queryForList(Dict.class, sql);
        final List<Dict> mapResultList = CollectionUtil.newArrayList();

        dao.each(Dict.class, sql, new Callback<Dict>() {
            @Override
            public void invoke(Dict obj) {
                mapResultList.add(obj);
            }
        });
        Assert.assertTrue(mapResultList.containsAll(mapList));

        sql = SqlBuilders.select(TestEntity.class).column("name").create();
        final List<String> nameList = dao.queryForList(String.class, sql);
        final List<String> nameResultList = CollectionUtil.newArrayList();

        dao.each(String.class, sql, new Callback<String>() {
            @Override
            public void invoke(String obj) {
                nameResultList.add(obj);
            }
        });

        Assert.assertTrue(nameResultList.containsAll(nameList));
        Assert.assertArrayEquals(new int[]{1, 1}, dao.delete(entities));
    }

    @Test
    public void execute() {
        TestEntity entity = insertEntity(dao);
        Sql sql = SqlBuilders.delete(TestEntity.class)
                .where("id", "=", entity.getId())
                .create();
        int[] result = dao.execute(CollectionUtil.newArrayList(sql, sql));
        Assert.assertEquals("[1, 0]", Arrays.toString(result));
    }

    @Test
    public void sqlKey() {
        initSqlContent();

        TestEntity entity = insertEntity(dao);

        TestEntity result = dao.queryForObject(TestEntity.class,
                SqlBuilders.sqlKey("t1")
                        .setParameter("name", entity.getName())
                        .create()
        );

        Assert.assertEquals(entity, result);

        Assert.assertNull(dao.queryForObject(TestEntity.class,
                SqlBuilders.sqlKey("t1")
                        .setParameter("name", "Not " + entity.getName())
                        .create()
        ));
    }

    @After
    public void after() {
        dao.execute(SqlBuilders.delete(TestEntity.class).create());
    }

    private TestEntity insertEntity(Dao dao) {
        return dao.insert(new TestEntity()
                .setName("x")
                .setRemark("y"));
    }
}