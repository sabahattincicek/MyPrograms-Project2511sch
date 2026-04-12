package com.saboon.project_2511sch.data.repository

import com.google.common.truth.Truth.assertThat
import com.saboon.project_2511sch.data.local.dao.UserDao
import com.saboon.project_2511sch.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class TestUserRepository : TestBaseRepository() {

    private val userDao = mockk<UserDao>(relaxed = true)
    private lateinit var userRepository: UserRepositoryImp

    val testUser = baseUser.copy(id = "1", userName = "myprog", email = "test@myprog.com", isActive = true)

    @Before
    fun setup() {
        userRepository = UserRepositoryImp(userDao)
    }

    @Test
    fun `insert user successfully returns Success`() = runTest {
        // GIVEN: DAO'nun başarılı olduğunu varsayıyoruz

        // WHEN: Repository'deki insert'ü çağırıyoruz
        val result = userRepository.insert(testUser)

        // THEN:
        // 1. Sonuç Success mi?
        assertThat(result).isInstanceOf(Resource.Success::class.java)

        // 2. DAO'nun insert fonksiyonu gerçekten çağrıldı mı?
        coVerify { userDao.insert(any()) }
    }

    @Test
    fun `insert user failure returns Error`() = runTest {
        // GIVEN: DAO hata fırlatırsa ne olur?
        coEvery { userDao.insert(any()) } throws Exception("Database Error")

        // WHEN
        val result = userRepository.insert(testUser)

        // THEN
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(result.message).isEqualTo("Database Error")
    }

    @Test
    fun `getActive returns Success when user found`() = runTest {
        // GIVEN: DAO'dan bir UserEntity akışı (flow) geleceğini öğretiyoruz
        val userEntity = baseUserEntity.copy(id = "1", userName = "Sabo", email = "sabo@test.com", isActive = true)
        coEvery { userDao.getActive() } returns flowOf(userEntity)

        // WHEN: Repository'den veriyi çekiyoruz
        val result = userRepository.getActive().first()

        // THEN: Entity'nin Domain modele (User) dönüştüğünü kontrol et
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat(result.data?.userName).isEqualTo("Sabo")
    }

    @Test
    fun `getActive returns Error when flow fails`() = runTest {
        // GIVEN: Flow içinde bir hata fırlatılırsa (catch bloğunu test ediyoruz)
        // catch bloğunu tetiklemek için hata fırlatan bir flow:
        coEvery { userDao.getActive() } returns flow { throw Exception("Flow Error") }

        // WHEN
        val result = userRepository.getActive().first()

        // THEN
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(result.message).isEqualTo("Flow Error")
    }
}