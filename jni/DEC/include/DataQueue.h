#pragma once

#include <list>
template<class T>
class DataQueue
{
protected:
	std::list<T> m_list;
    pthread_mutex_t m_mux;
    pthread_cond_t m_cond;
	bool m_bExit;
public:
	DataQueue()
	{
        pthread_mutex_init(&m_mux,0);
        pthread_cond_init(&m_cond,0);
		m_bExit = false;
	}
	~DataQueue()
	{
		pthread_mutex_destroy(&m_mux);
		pthread_cond_destroy(&m_cond);
	}
	void PostData(T d)
	{
		pthread_mutex_lock(&m_mux);
		m_list.push_back(d);
		if(m_list.size() > 0)
		{
			 pthread_cond_broadcast(&m_cond);
		}
		pthread_mutex_unlock(&m_mux);
	}
	int GetData(T &d)
	{
		int ret = 0;
		pthread_mutex_lock(&m_mux);
		while(m_list.size() <= 0 && !m_bExit)
		{
			pthread_cond_wait(&m_cond,&m_mux);
		}
		if(m_list.size() > 0)
		{
			d = m_list.front();
			m_list.pop_front();
		}
		if(m_bExit)
		{
			ret = 1;
		}
		pthread_mutex_unlock(&m_mux);
		return ret;
	}
	int GetDataBackNoWait(T &d)
	{
		int ret = 0;
		pthread_mutex_lock(&m_mux);
		if(m_list.size() > 0)
		{
			d = m_list.back();
			m_list.pop_back();
		}
		if(m_bExit)
		{
			ret = 1;
		}
		pthread_mutex_unlock(&m_mux);
		return ret;
	}
	int PeekData(T &d)
	{
		int ret = 0;
		pthread_mutex_lock(&m_mux);
		while(m_list.size() <= 0 && !m_bExit)
		{
			pthread_cond_wait(&m_cond,&m_mux);
		}
		if(m_list.size() > 0)
		{
			d = m_list.front();
		}
		if(m_bExit)
		{
			ret = 1;
		}
		pthread_mutex_unlock(&m_mux);
		return ret;
	}
	int Size()
	{
		int n = 0;
		pthread_mutex_lock(&m_mux);
		n = m_list.size();
		pthread_mutex_unlock(&m_mux);
		return n;
	}
	void Exit()
	{
		pthread_mutex_lock(&m_mux);
		m_bExit = true;
		pthread_cond_broadcast(&m_cond);
		pthread_mutex_unlock(&m_mux);
	}
	void SetExit(bool b)
	{
		pthread_mutex_lock(&m_mux);
		m_bExit = b;
		pthread_mutex_unlock(&m_mux);
	}
	void Clear()
	{
		pthread_mutex_lock(&m_mux);
		m_list.clear();
		pthread_mutex_unlock(&m_mux);
	}
};
