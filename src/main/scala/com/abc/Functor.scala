package com.abc

trait F[_] {
  def map[A, B](f: A => B): F[B]
}

trait M[_] {
  def map[A, B](f: A => B): M[B]

  def flatMap[A, B](f: A => M[B]): M[B]
}
